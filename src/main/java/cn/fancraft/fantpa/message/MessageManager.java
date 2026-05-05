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

    private MessageManager() {}

    public static void send(ServerPlayer player, String key) {
        send(player, key, null, null);
    }

    public static void send(ServerPlayer player, String key, Map<String, String> placeholders) {
        send(player, key, placeholders, null);
    }

    public static void send(ServerPlayer player, String key, Map<String, String> placeholders, String clickCommand) {
        if (player == null) return;
        MutableComponent c = parseColors(localized(player, key, placeholders));
        if (clickCommand != null)
            c.setStyle(c.getStyle().withClickEvent(new ClickEvent.RunCommand(clickCommand)));
        player.sendSystemMessage(c);
    }

    public static void sendSuccess(ServerPlayer player, String key) { sendWithPrefix(player, "prefix.success", ChatFormatting.GREEN, "[✔] ", key, null); }

    public static void sendSuccess(ServerPlayer player, String key, Map<String, String> ph) { sendWithPrefix(player, "prefix.success", ChatFormatting.GREEN, "[✔] ", key, ph); }

    public static void sendError(ServerPlayer player, String key) { sendWithPrefix(player, "prefix.error", ChatFormatting.RED, "[✘] ", key, null); }

    public static void sendError(ServerPlayer player, String key, Map<String, String> ph) { sendWithPrefix(player, "prefix.error", ChatFormatting.RED, "[✘] ", key, ph); }

    private static void sendWithPrefix(ServerPlayer player, String prefixKey, ChatFormatting defaultColor, String defaultSymbol, String key, Map<String, String> ph) {
        if (player == null) return;
        MutableComponent prefix = LangManager.getInstance().hasKey(prefixKey)
            ? parseColors(localized(player, prefixKey))
            : Component.literal(defaultSymbol).withStyle(defaultColor);
        player.sendSystemMessage(prefix.append(parseColors(localized(player, key, ph))));
    }

    /** 发送一行内两个可点击按钮 */
    public static void sendAcceptDenyButtons(ServerPlayer player, String acceptCmd, String denyCmd) {
        if (player == null) return;
        String locale = LangManager.getPlayerLocale(player);
        MutableComponent line = Component.empty()
            .append(parseColors(LangManager.getInstance().get(locale, "tpa.accept_button"))
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(acceptCmd))));
        String sep = LangManager.getInstance().get(locale, "tpa.button_separator");
        if (!sep.isEmpty()) line.append(parseColors(sep));
        line.append(parseColors(LangManager.getInstance().get(locale, "tpa.deny_button"))
            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(denyCmd))));
        player.sendSystemMessage(line);
    }

    public static void sendClickable(ServerPlayer player, String key, Map<String, String> ph, String clickCmd, String hoverKey) {
        if (player == null) return;
        MutableComponent c = parseColors(localized(player, key, ph))
            .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(clickCmd)));
        if (hoverKey != null)
            c.setStyle(c.getStyle().withHoverEvent(new HoverEvent.ShowText(parseColors(localized(player, hoverKey)))));
        player.sendSystemMessage(c);
    }

    /** 发送给控制台或玩家的反馈消息 */
    public static void sendFeedback(net.minecraft.commands.CommandSourceStack src, String key) {
        sendFeedback(src, key, null);
    }

    public static void sendFeedback(net.minecraft.commands.CommandSourceStack src, String key, Map<String, String> ph) {
        ServerPlayer player = null;
        try { player = src.getPlayer(); } catch (Exception ignored) {}
        if (player != null) {
            sendSuccess(player, key, ph);
        } else {
            String msg = LangManager.getInstance().get(key, ph);
            src.sendSuccess(() -> Component.literal(msg.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "")), false);
        }
    }

    public static MutableComponent parseColors(String text) {
        if (text == null) return Component.empty();
        String processed = Pattern.compile("&#[0-9a-fA-F]{6}").matcher(text).replaceAll("");

        MutableComponent result = Component.empty();
        int lastIndex = 0;
        Matcher m = COLOR_PATTERN.matcher(processed);
        Style currentStyle = Style.EMPTY;

        while (m.find()) {
            if (m.start() > lastIndex)
                result.append(Component.literal(processed.substring(lastIndex, m.start())).setStyle(currentStyle));
            currentStyle = applyLegacyCode(m.group(1).toLowerCase().charAt(0));
            lastIndex = m.end();
        }
        if (lastIndex < processed.length())
            result.append(Component.literal(processed.substring(lastIndex)).setStyle(currentStyle));
        return result;
    }

    private static Style applyLegacyCode(char code) {
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
            default -> Style.EMPTY;
        };
    }

    private static String localized(ServerPlayer player, String key) {
        return LangManager.getInstance().get(LangManager.getPlayerLocale(player), key);
    }

    private static String localized(ServerPlayer player, String key, Map<String, String> ph) {
        return LangManager.getInstance().get(LangManager.getPlayerLocale(player), key, ph);
    }
}
