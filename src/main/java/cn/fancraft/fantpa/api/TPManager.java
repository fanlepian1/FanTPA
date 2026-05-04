package cn.fancraft.fantpa.api;

import cn.fancraft.fantpa.Fantpa;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class TPManager {

    public static boolean teleportToPlayer(ServerPlayer player, ServerPlayer target) {
        return player.teleportTo(
            (ServerLevel) target.level(),
            target.getX(), target.getY(), target.getZ(),
            Set.of(),
            target.getXRot(), target.getYRot(),
            false
        );
    }

    public static boolean teleportToPosition(ServerPlayer player, GlobalPos pos) {
        ServerLevel level = Fantpa.getServerInstance().getLevel(pos.dimension());
        if (level == null) return false;
        return player.teleportTo(
            level,
            pos.pos().getX() + 0.5, pos.pos().getY(), pos.pos().getZ() + 0.5,
            Set.of(),
            player.getXRot(), player.getYRot(),
            false
        );
    }

    public static boolean teleportToDeathPoint(ServerPlayer player, GlobalPos deathPos) {
        ServerLevel level = Fantpa.getServerInstance().getLevel(deathPos.dimension());
        if (level == null) return false;
        return player.teleportTo(
            level,
            deathPos.pos().getX() + 0.5,
            deathPos.pos().getY() + 0.05,
            deathPos.pos().getZ() + 0.5,
            Set.of(),
            player.getXRot(), player.getYRot(),
            false
        );
    }
}
