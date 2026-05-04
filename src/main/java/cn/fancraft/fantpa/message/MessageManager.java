package cn.fancraft.fantpa.message;

import cn.fancraft.fantpa.lang.LangManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fA-Fk-oK-OrR])");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    private MessageManager() {}

    public static void send(ServerPlayer player, String key) {
        send(player, key, null, null);
    }

    public static void send(ServerPlayer player, String key, Map<String, String> placeholders) {
        send(player, key, placeholders, null);
    }

    public static void send(ServerPlayer player, String key, Map<String, String> placeholders, String clickCommand) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);
        String raw = LangManager.getInstance().get(locale, key, placeholders);
        MutableComponent c = parseColors(raw);
        if (clickCommand != null)
            c.setStyle(c.getStyle().withClickEvent(new ClickEvent.RunCommand(clickCommand)));
        player.sendSystemMessage(c);
    }

    public static void sendSuccess(ServerPlayer player, String key) {
        sendSuccess(player, key, null);
    }

    public static void sendSuccess(ServerPlayer player, String key, Map<String, String> placeholders) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);
        String raw = LangManager.getInstance().get(locale, key, placeholders);
        MutableComponent prefix = LangManager.getInstance().hasKey("prefix.success")
            ? parseColors(LangManager.getInstance().get(locale, "prefix.success"))
            : Component.literal("[✔] ").withStyle(ChatFormatting.GREEN);
        player.sendSystemMessage(prefix.append(parseColors(raw)));
    }

    public static void sendError(ServerPlayer player, String key) {
        sendError(player, key, null);
    }

    public static void sendError(ServerPlayer player, String key, Map<String, String> placeholders) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);
        String raw = LangManager.getInstance().get(locale, key, placeholders);
        MutableComponent prefix = LangManager.getInstance().hasKey("prefix.error")
            ? parseColors(LangManager.getInstance().get(locale, "prefix.error"))
            : Component.literal("[✘] ").withStyle(ChatFormatting.RED);
        player.sendSystemMessage(prefix.append(parseColors(raw)));
    }

    /** 发送一行内两个可点击按钮，左边 accept，右边 deny */
    public static void sendAcceptDenyButtons(ServerPlayer player, String acceptCmd, String denyCmd) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);

        String leftText = LangManager.getInstance().get(locale, "tpa.accept_button");
        String rightText = LangManager.getInstance().get(locale, "tpa.deny_button");
        String sep = LangManager.getInstance().get(locale, "tpa.button_separator");

        MutableComponent line = Component.empty()
            .append(parseColors(leftText)
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(acceptCmd))));

        if (!sep.isEmpty())
            line.append(parseColors(sep));

        line.append(parseColors(rightText)
            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(denyCmd))));

        player.sendSystemMessage(line);
    }

    public static void sendClickable(ServerPlayer player, String key, Map<String, String> placeholders,
                                      String clickCommand, String hoverKey) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);
        String raw = LangManager.getInstance().get(locale, key, placeholders);
        MutableComponent c = parseColors(raw)
            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(clickCommand)));
        if (hoverKey != null) {
            String hoverText = LangManager.getInstance().get(locale, hoverKey);
            c.setStyle(c.getStyle().withHoverEvent(new HoverEvent.ShowText(parseColors(hoverText))));
        }
        player.sendSystemMessage(c);
    }

    public static MutableComponent parseColors(String text) {
        if (text == null) return Component.empty();
        String processed = HEX_PATTERN.matcher(text).replaceAll("");

        MutableComponent result = Component.empty();
        int lastIndex = 0;
        Matcher m = COLOR_PATTERN.matcher(processed);
        Style currentStyle = Style.EMPTY;

        while (m.find()) {
            if (m.start() > lastIndex)
                result.append(Component.literal(processed.substring(lastIndex, m.start())).setStyle(currentStyle));
            currentStyle = applyLegacyCode(currentStyle, m.group(1).toLowerCase().charAt(0));
            lastIndex = m.end();
        }
        if (lastIndex < processed.length())
            result.append(Component.literal(processed.substring(lastIndex)).setStyle(currentStyle));
        return result;
    }

    private static Style applyLegacyCode(Style base, char code) {
        return switch (code) {
            case '0' -> Style.EMPTY.withColor(ChatFormatting.BLACK);
            case '1' -> Style.EMPTY.withColor(ChatFormatting.DARK_BLUE);
            case '2' -> Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
            case '3' -> Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);
            case '4' -> Style.EMPTY.withColor(ChatFormatting.DARK_RED);
            case '5' -> Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);
            case '6' -> Style.EMPTY.withColor(ChatFormatting.GOLD);
            case '7' -> Style.EMPTY.withColor(ChatFormatting.GRAY);
            case '8' -> Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
            case '9' -> Style.EMPTY.withColor(ChatFormatting.BLUE);
            case 'a' -> Style.EMPTY.withColor(ChatFormatting.GREEN);
            case 'b' -> Style.EMPTY.withColor(ChatFormatting.AQUA);
            case 'c' -> Style.EMPTY.withColor(ChatFormatting.RED);
            case 'd' -> Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e' -> Style.EMPTY.withColor(ChatFormatting.YELLOW);
            case 'f' -> Style.EMPTY.withColor(ChatFormatting.WHITE);
            case 'k' -> Style.EMPTY.withObfuscated(true);
            case 'l' -> Style.EMPTY.withBold(true);
            case 'm' -> Style.EMPTY.withStrikethrough(true);
            case 'n' -> Style.EMPTY.withUnderlined(true);
            case 'o' -> Style.EMPTY.withItalic(true);
            case 'r' -> Style.EMPTY;
            default -> base;
        };
    }
}
