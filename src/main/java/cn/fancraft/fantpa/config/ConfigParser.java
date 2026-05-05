package cn.fancraft.fantpa.config;

public class ConfigParser {

    public static ConfigData parse(String content) {
        ConfigData d = new ConfigData();
        d.teleportTimeout = extractInt(content, "timeout", 30);
        d.teleportCooldown = extractInt(content, "cooldown", 5);
        d.teleportDelay = extractInt(content, "delay", 3);
        d.defaultLanguage = extractString(content, "default-language", "en_us");
        d.tpaEnabled = extractBool(content, "tpa.*?enabled", true);
        d.tpahereEnabled = extractBool(content, "tpahere.*?enabled", true);
        d.backEnabled = extractBool(content, "back.*?enabled", true);
        d.homeEnabled = extractBool(content, "home.*?enabled", true);
        d.sethomeEnabled = extractBool(content, "sethome.*?enabled", true);
        d.delhomeEnabled = extractBool(content, "delhome.*?enabled", true);
        return d;
    }

    public static String serialize(ConfigData c) {
        return """
            fantpa {
                teleport { timeout = %d  cooldown = %d  delay = %d }
                language { default-language = "%s" }
                commands {
                    tpa { enabled = %s }  tpahere { enabled = %s }  back { enabled = %s }
                    home { enabled = %s }  sethome { enabled = %s }  delhome { enabled = %s }
                }
            }
            """.formatted(c.teleportTimeout, c.teleportCooldown, c.teleportDelay,
                c.defaultLanguage, c.tpaEnabled, c.tpahereEnabled,
                c.backEnabled, c.homeEnabled, c.sethomeEnabled, c.delhomeEnabled);
    }

    private static int extractInt(String content, String key, int def) {
        try { var m = java.util.regex.Pattern.compile(key + "\\s*=\\s*(\\d+)").matcher(content);
              if (m.find()) return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        return def;
    }

    private static boolean extractBool(String content, String key, boolean def) {
        try { return java.util.regex.Pattern.compile("(?s)" + key + ".*?=\\s*(true|false)", java.util.regex.Pattern.DOTALL)
              .matcher(content).results().map(r -> Boolean.parseBoolean(r.group(1))).findFirst().orElse(def); }
        catch (Exception ignored) { return def; }
    }

    private static String extractString(String content, String key, String def) {
        try { return java.util.regex.Pattern.compile("(?s)" + key + "\\s*=\\s*\"([^\"]+)\"", java.util.regex.Pattern.DOTALL)
              .matcher(content).results().map(r -> r.group(1)).findFirst().orElse(def); }
        catch (Exception ignored) { return def; }
    }
}
