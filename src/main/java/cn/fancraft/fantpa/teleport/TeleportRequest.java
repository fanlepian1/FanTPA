package cn.fancraft.fantpa.teleport;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class TeleportRequest {

    public enum Type {
        TPA,      // 请求传送到对方
        TPAHERE   // 请求对方传送到自己
    }

    public enum State {
        PENDING,
        ACCEPTED,
        DENIED,
        TIMEOUT,
        COMPLETED
    }

    private final UUID id;
    private final ServerPlayer sender;
    private final ServerPlayer target;
    private final Type type;
    private State state;
    private final long createdAt;

    public TeleportRequest(ServerPlayer sender, ServerPlayer target, Type type) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.target = target;
        this.type = type;
        this.state = State.PENDING;
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getId() { return id; }

    public ServerPlayer getSender() { return sender; }

    public ServerPlayer getTarget() { return target; }

    public Type getType() { return type; }

    public State getState() { return state; }

    public void setState(State state) { this.state = state; }

    public long getCreatedAt() { return createdAt; }

    public boolean isExpired(int timeoutSeconds) {
        return System.currentTimeMillis() - createdAt > timeoutSeconds * 1000L;
    }

    public ServerPlayer getTeleportPlayer() {
        return type == Type.TPA ? sender : target;
    }

    public ServerPlayer getDestinationPlayer() {
        return type == Type.TPA ? target : sender;
    }
}
