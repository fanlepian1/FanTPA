package cn.fancraft.fantpa.api;

import cn.fancraft.fantpa.Fantpa;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;

public class PlayerManager {

    private static String[] allOPs;

    public static String[] getAllOPs() {
        if (allOPs == null && Fantpa.getServerInstance() != null) {
            allOPs = Fantpa.getServerInstance().getPlayerList().getOps().getUserList();
        }
        return allOPs;
    }

    public static boolean isPlayerOnline(String playerName) {
        return Fantpa.getServerInstance().getPlayerList().getPlayer(playerName) != null;
    }

    public static ServerPlayer getPlayer(String playerName) {
        return Fantpa.getServerInstance().getPlayerList().getPlayer(playerName);
    }

    public static boolean isPlayerOP(String playerName) {
        if (allOPs == null) loadOPs();
        if (allOPs == null) return false;
        for (String opName : allOPs) {
            if (opName.equalsIgnoreCase(playerName)) return true;
        }
        return false;
    }

    public static void loadOPs() {
        if (Fantpa.getServerInstance() != null) {
            allOPs = Fantpa.getServerInstance().getPlayerList().getOps().getUserList();
        }
    }

    /** 使用 Minecraft 内置死亡位置记录，无需自行存储 */
    public static GlobalPos getLastDeathPosition(ServerPlayer player) {
        return player.getLastDeathLocation().orElse(null);
    }

    public static boolean hasDeathLocation(ServerPlayer player) {
        return player.getLastDeathLocation().isPresent();
    }
}
