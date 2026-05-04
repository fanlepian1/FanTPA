package cn.fancraft.fantpa.config;

public class ConfigParser {

    public static ConfigData parse(String content) {
        ConfigData data = new ConfigData();

        data.teleportTimeout = extractInt(content, "timeout", 30);
        data.teleportCooldown = extractInt(content, "cooldown", 5);
        data.teleportDelay = extractInt(content, "delay", 3);
        data.defaultLanguage = extractString(content, "default-language", "en_us");

        data.tpaEnabled = extractBool(content, "tpa.*?enabled", true);
        data.tpahereEnabled = extractBool(content, "tpahere.*?enabled", true);
        data.backEnabled = extractBool(content, "back.*?enabled", true);
        data.homeEnabled = extractBool(content, "home.*?enabled", true);

        data.tpaPermission = extractString(content, "permissions.*?tpa", "fantpa.command.tpa");
        data.tpaherePermission = extractString(content, "permissions.*?tpahere", "fantpa.command.tpahere");
        data.backPermission = extractString(content, "permissions.*?back", "fantpa.command.back");
        data.homePermission = extractString(content, "permissions.*?home", "fantpa.command.home");
        data.adminPermission = extractString(content, "permissions.*?admin", "fantpa.admin");
        data.tpallPermission = extractString(content, "permissions.*?tpall", "fantpa.admin.tpall");

        return data;
    }

    public static String serialize(ConfigData config) {
        return """
            fantpa {
                teleport {
                    timeout = %d
                    cooldown = %d
                    delay = %d
                }
                language {
                    default-language = "%s"
                }
                commands {
                    tpa { enabled = %s }
                    tpahere { enabled = %s }
                    back { enabled = %s }
                    home { enabled = %s }
                    sethome { enabled = %s }
                    delhome { enabled = %s }
                }
                permissions {
                    tpa = "%s"
                    tpahere = "%s"
                    back = "%s"
                    home = "%s"
                    sethome = "%s"
                    delhome = "%s"
                    tpall = "%s"
                    admin = "%s"
                }
            }
            """.formatted(
                config.teleportTimeout, config.teleportCooldown, config.teleportDelay,
                config.defaultLanguage,
                config.tpaEnabled, config.tpahereEnabled,
                config.backEnabled, config.homeEnabled,
                config.sethomeEnabled, config.delhomeEnabled,
                config.tpaPermission, config.tpaherePermission,
                config.backPermission, config.homePermission,
                config.sethomePermission, config.delhomePermission,
                config.tpallPermission,
                config.adminPermission
            );
    }

    private static int extractInt(String content, String key, int defaultValue) {
        try {
            var m = java.util.regex.Pattern.compile(key + "\\s*=\\s*(\\d+)").matcher(content);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {}
        return defaultValue;
    }

    private static boolean extractBool(String content, String keyPattern, boolean defaultValue) {
        try {
            return java.util.regex.Pattern.compile(
                "(?s)" + keyPattern + ".*?=\\s*(true|false)", java.util.regex.Pattern.DOTALL
            ).matcher(content).results()
              .map(r -> Boolean.parseBoolean(r.group(1)))
              .findFirst().orElse(defaultValue);
        } catch (Exception ignored) {}
        return defaultValue;
    }

    private static String extractString(String content, String keyPattern, String defaultValue) {
        try {
            return java.util.regex.Pattern.compile(
                "(?s)" + keyPattern + "\\s*=\\s*\"([^\"]+)\"", java.util.regex.Pattern.DOTALL
            ).matcher(content).results()
              .map(r -> r.group(1))
              .findFirst().orElse(defaultValue);
        } catch (Exception ignored) {}
        return defaultValue;
    }
}
