package cn.fancraft.fantpa.event;

import net.minecraft.server.level.ServerPlayer;

public class TeleportEvents {

    private TeleportEvents() {}

    public record TpaEvent(ServerPlayer sender, ServerPlayer target, Type type) {
        public enum Type { REQUEST, ACCEPT, DENY, HERE_REQUEST }
    }

    public record TeleportEvent(ServerPlayer player, ServerPlayer target, String teleportType) {}

    public record BackEvent(ServerPlayer player) {}

    public record HomeEvent(ServerPlayer player, String homeName, Type type) {
        public enum Type { SET, TELEPORT }
    }
}
