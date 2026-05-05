package cn.fancraft.fantpa.utils;

import cn.fancraft.fantpa.Fantpa;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;

public class PlayerManager {

    private static String[] allOPs;

    public static boolean isPlayerOnline(String playerName) {
        return Fantpa.getServerInstance().getPlayerList().getPlayer(playerName) != null;
    }

    public static ServerPlayer getPlayer(String playerName) {
        return Fantpa.getServerInstance().getPlayerList().getPlayer(playerName);
    }

    public static boolean isPlayerOP(String playerName) {
        if (allOPs == null && Fantpa.getServerInstance() != null)
            allOPs = Fantpa.getServerInstance().getPlayerList().getOps().getUserList();
        if (allOPs == null) return false;
        for (String op : allOPs) if (op.equalsIgnoreCase(playerName)) return true;
        return false;
    }

    public static GlobalPos getLastDeathPosition(ServerPlayer player) {
        return player.getLastDeathLocation().orElse(null);
    }
}
