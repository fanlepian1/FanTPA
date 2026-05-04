package cn.fancraft.fantpa.lang;

import cn.fancraft.fantpa.Fantpa;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.utils.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private static final LangManager INSTANCE = new LangManager();
    private static final Gson GSON = new Gson();
    private static final Path LANG_DIR = Paths.get("config", "fantpa", "lang");
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static final String[] BUILTIN_LOCALES = {
        "en_us", "zh_cn", "ru_ru", "ja_jp", "ko_kr",
        "fr_fr", "de_de", "es_es", "pt_br"
    };

    private final Map<String, Map<String, String>> localeMap = new HashMap<>();
    private Map<String, String> fallback = new HashMap<>();

    public static LangManager getInstance() { return INSTANCE; }

    public void loadLanguages() {
        localeMap.clear();
        fallback = loadBuiltin(getDefaultLocale());

        try { Files.createDirectories(LANG_DIR); }
        catch (IOException e) { LoggerUtil.error("无法创建语言文件目录", e); }

        for (String locale : BUILTIN_LOCALES) {
            Path target = LANG_DIR.resolve(locale + ".json");
            if (Files.notExists(target)) copyBuiltinToConfig(locale, target);
        }

        try {
            for (Path file : Files.list(LANG_DIR).toList()) {
                String name = file.getFileName().toString();
                if (name.endsWith(".json")) {
                    String locale = name.substring(0, name.length() - 5);
                    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        Map<String, String> map = GSON.fromJson(reader, MAP_TYPE);
                        if (map != null) localeMap.put(locale, map);
                    }
                }
            }
            LoggerUtil.info("语言文件加载完成，可用语言: " + localeMap.keySet());
        } catch (IOException e) {
            LoggerUtil.error("扫描语言文件目录失败", e);
        }
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
        Map<String, String> localeMessages = localeMap.get(locale);
        if (localeMessages != null && localeMessages.containsKey(key))
            return localeMessages.get(key);
        return fallback.getOrDefault(key, key);
    }

    public boolean hasKey(String key) {
        for (Map<String, String> map : localeMap.values())
            if (map.containsKey(key)) return true;
        return fallback.containsKey(key);
    }

    public void reload() { loadLanguages(); }

    public static String getPlayerLocale(net.minecraft.server.level.ServerPlayer player) {
        try { return player.clientInformation().language(); }
        catch (Exception e) { return ConfigManager.getInstance().getConfig().defaultLanguage; }
    }

    private Map<String, String> loadBuiltin(String locale) {
        String path = "/assets/fantpa/lang/" + locale + ".json";
        try (InputStream in = Fantpa.class.getResourceAsStream(path)) {
            if (in == null) {
                if (!"en_us".equals(locale))
                    return loadBuiltin("en_us");
                return new HashMap<>();
            }
            Map<String, String> result = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), MAP_TYPE);
            return result != null ? result : new HashMap<>();
        } catch (IOException e) {
            LoggerUtil.error("加载内置语言文件失败: " + path, e);
            return new HashMap<>();
        }
    }

    private void copyBuiltinToConfig(String locale, Path target) {
        String path = "/assets/fantpa/lang/" + locale + ".json";
        try (InputStream in = Fantpa.class.getResourceAsStream(path)) {
            if (in == null) return;
            Files.copy(in, target);
        } catch (IOException ignored) {}
    }

    private String replacePlaceholders(String msg, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return msg;
        for (Map.Entry<String, String> e : placeholders.entrySet())
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        return msg;
    }
}
