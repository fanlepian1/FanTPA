package cn.fancraft.fantpa.event;

import net.minecraft.server.level.ServerPlayer;

/**
 * 传送相关事件
 */
public class TeleportEvents {

    private TeleportEvents() {}

    public static class TpaRequestEvent {
        private final ServerPlayer sender;
        private final ServerPlayer target;

        public TpaRequestEvent(ServerPlayer sender, ServerPlayer target) {
            this.sender = sender;
            this.target = target;
        }

        public ServerPlayer getSender() { return sender; }
        public ServerPlayer getTarget() { return target; }
    }

    public static class TpaAcceptEvent {
        private final ServerPlayer sender;
        private final ServerPlayer target;

        public TpaAcceptEvent(ServerPlayer sender, ServerPlayer target) {
            this.sender = sender;
            this.target = target;
        }

        public ServerPlayer getSender() { return sender; }
        public ServerPlayer getTarget() { return target; }
    }

    public static class TpaDenyEvent {
        private final ServerPlayer sender;
        private final ServerPlayer target;

        public TpaDenyEvent(ServerPlayer sender, ServerPlayer target) {
            this.sender = sender;
            this.target = target;
        }

        public ServerPlayer getSender() { return sender; }
        public ServerPlayer getTarget() { return target; }
    }

    public static class TpaHereRequestEvent {
        private final ServerPlayer sender;
        private final ServerPlayer target;

        public TpaHereRequestEvent(ServerPlayer sender, ServerPlayer target) {
            this.sender = sender;
            this.target = target;
        }

        public ServerPlayer getSender() { return sender; }
        public ServerPlayer getTarget() { return target; }
    }

    public static class TeleportEvent {
        private final ServerPlayer player;
        private final ServerPlayer target;
        private final String teleportType; // "tpa", "tpahere", "back", "home", "death"

        public TeleportEvent(ServerPlayer player, ServerPlayer target, String teleportType) {
            this.player = player;
            this.target = target;
            this.teleportType = teleportType;
        }

        public ServerPlayer getPlayer() { return player; }
        public ServerPlayer getTarget() { return target; }
        public String getTeleportType() { return teleportType; }
    }

    public static class BackEvent {
        private final ServerPlayer player;
        private final String backType; // "last_location", "death"

        public BackEvent(ServerPlayer player, String backType) {
            this.player = player;
            this.backType = backType;
        }

        public ServerPlayer getPlayer() { return player; }
        public String getBackType() { return backType; }
    }

    public static class HomeSetEvent {
        private final ServerPlayer player;
        private final String homeName;

        public HomeSetEvent(ServerPlayer player, String homeName) {
            this.player = player;
            this.homeName = homeName;
        }

        public ServerPlayer getPlayer() { return player; }
        public String getHomeName() { return homeName; }
    }

    public static class HomeTeleportEvent {
        private final ServerPlayer player;
        private final String homeName;

        public HomeTeleportEvent(ServerPlayer player, String homeName) {
            this.player = player;
            this.homeName = homeName;
        }

        public ServerPlayer getPlayer() { return player; }
        public String getHomeName() { return homeName; }
    }
}
