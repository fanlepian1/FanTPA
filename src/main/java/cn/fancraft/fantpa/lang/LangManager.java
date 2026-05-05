package cn.fancraft.fantpa.lang;

import cn.fancraft.fantpa.Fantpa;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.utils.LoggerUtil;
import cn.fancraft.fantpa.utils.PlayerDataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LangManager {

    private static final LangManager INSTANCE = new LangManager();
    private static final Gson GSON = new Gson();
    private static final Path LANG_DIR = Paths.get("config", "fantpa", "lang");
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private final Map<String, Map<String, String>> localeMap = new HashMap<>();
    private Map<String, String> fallback = new HashMap<>();

    public static LangManager getInstance() { return INSTANCE; }

    public void loadLanguages() {
        localeMap.clear();

        try { Files.createDirectories(LANG_DIR); }
        catch (IOException e) { LoggerUtil.error("Failed to create lang directory", e); }

        // 1. Copy builtin lang files from JAR to config/lang if missing
        copyBuiltinsToConfig();

        // 2. Load all .json files from config/lang
        try (var files = Files.list(LANG_DIR)) {
            files.filter(f -> f.toString().endsWith(".json")).forEach(f -> {
                String locale = f.getFileName().toString().replace(".json", "");
                try (Reader reader = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                    Map<String, String> map = GSON.fromJson(reader, MAP_TYPE);
                    if (map != null) localeMap.put(locale, map);
                } catch (IOException e) {
                    LoggerUtil.error("Failed to load lang file: " + f, e);
                }
            });
        } catch (IOException e) {
            LoggerUtil.error("Failed to scan lang directory", e);
        }

        // 3. Set fallback (try config default first, then en_us)
        String defaultLocale = getDefaultLocale();
        fallback = localeMap.getOrDefault(defaultLocale,
            localeMap.getOrDefault("en_us", new HashMap<>()));

        LoggerUtil.info("Languages loaded (" + localeMap.size() + "): " + localeMap.keySet());
    }

    /** Copy builtin lang files from JAR to config/lang if they don't already exist there. */
    private void copyBuiltinsToConfig() {
        for (String locale : discoverBuiltinLocales()) {
            Path target = LANG_DIR.resolve(locale + ".json");
            if (Files.notExists(target)) {
                String resourcePath = "/assets/fantpa/lang/" + locale + ".json";
                try (InputStream in = Fantpa.class.getResourceAsStream(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, target);
                        LoggerUtil.info("Copied builtin lang: " + locale);
                    }
                } catch (IOException ignored) {}
            }
        }
    }

    /** Discover all builtin locale codes from the JAR's assets/fantpa/lang/ directory. */
    private Set<String> discoverBuiltinLocales() {
        Set<String> locales = new LinkedHashSet<>();
        try {
            URL url = Fantpa.class.getResource("/assets/fantpa/lang/");
            if (url == null) return locales;

            if ("jar".equals(url.getProtocol())) {
                try (FileSystem fs = FileSystems.newFileSystem(url.toURI(), Map.of())) {
                    Path langPath = fs.getPath("/assets/fantpa/lang/");
                    try (var files = Files.list(langPath)) {
                        files.filter(f -> f.toString().endsWith(".json"))
                            .map(f -> f.getFileName().toString().replace(".json", ""))
                            .forEach(locales::add);
                    }
                }
            } else {
                Path langPath = Paths.get(url.toURI());
                try (var files = Files.list(langPath)) {
                    files.filter(f -> f.toString().endsWith(".json"))
                        .map(f -> f.getFileName().toString().replace(".json", ""))
                        .forEach(locales::add);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Failed to discover builtin locales, falling back to en_us only", e);
        }

        if (locales.isEmpty()) locales.add("en_us");
        return locales;
    }

    public Set<String> getAvailableLocales() {
        return Collections.unmodifiableSet(localeMap.keySet());
    }

    private String getDefaultLocale() {
        try {
            var cfg = ConfigManager.getInstance().getConfig();
            if (cfg != null && cfg.defaultLanguage != null && !cfg.defaultLanguage.isBlank())
                return cfg.defaultLanguage;
        } catch (Exception ignored) {}
        return "en_us";
    }

    public String get(String key) {
        return getWithLocale(getDefaultLocale(), key);
    }

    public String get(String key, Map<String, String> placeholders) {
        return replacePlaceholders(get(key), placeholders);
    }

    public String get(String locale, String key) {
        return getWithLocale(locale, key);
    }

    public String get(String locale, String key, Map<String, String> placeholders) {
        return replacePlaceholders(getWithLocale(locale, key), placeholders);
    }

    private String getWithLocale(String locale, String key) {
        Map<String, String> messages = localeMap.get(locale);
        if (messages != null && messages.containsKey(key))
            return messages.get(key);
        return fallback.getOrDefault(key, key);
    }

    public boolean hasKey(String key) {
        for (Map<String, String> map : localeMap.values())
            if (map.containsKey(key)) return true;
        return fallback.containsKey(key);
    }

    public void reload() { loadLanguages(); }

    /**
     * Resolve player's language. Each tier is checked for availability before use.
     * Priority: personal choice > config default > client language > en_us
     */
    public static String getPlayerLocale(net.minecraft.server.level.ServerPlayer player) {
        String locale;

        // 1. Player's personal choice
        try {
            locale = PlayerDataManager.getInstance().getLanguage(player.getUUID());
            if (locale != null && !locale.isBlank() && INSTANCE.localeMap.containsKey(locale))
                return locale;
        } catch (Exception ignored) {}

        // 2. Config file default
        try {
            locale = ConfigManager.getInstance().getConfig().defaultLanguage;
            if (locale != null && !locale.isBlank() && INSTANCE.localeMap.containsKey(locale))
                return locale;
        } catch (Exception ignored) {}

        // 3. Player's client language
        try {
            locale = player.clientInformation().language();
            if (locale != null && !locale.isBlank() && INSTANCE.localeMap.containsKey(locale))
                return locale;
        } catch (Exception ignored) {}

        // 4. Fallback
        if (INSTANCE.localeMap.containsKey("en_us")) return "en_us";
        return INSTANCE.localeMap.keySet().iterator().next();
    }

    private String replacePlaceholders(String msg, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return msg;
        for (Map.Entry<String, String> e : placeholders.entrySet())
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        return msg;
    }
}
