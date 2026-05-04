package cn.fancraft.fantpa.event;

import cn.fancraft.fantpa.utils.LoggerUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {

    private static final EventManager INSTANCE = new EventManager();

    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public static EventManager getInstance() {
        return INSTANCE;
    }

    public <T> void register(Class<T> eventClass, EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <T> void unregister(Class<T> eventClass, EventListener<T> listener) {
        List<EventListener<?>> list = listeners.get(eventClass);
        if (list != null) {
            list.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void fire(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list != null) {
            for (EventListener<?> l : list) {
                try {
                    ((EventListener<T>) l).onEvent(event);
                } catch (Exception e) {
                    LoggerUtil.error("事件处理异常: " + event.getClass().getSimpleName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<Void> fireAsync(T event) {
        return CompletableFuture.runAsync(() -> {
            List<EventListener<?>> list = listeners.get(event.getClass());
            if (list != null) {
                for (EventListener<?> l : list) {
                    try {
                        ((EventListener<T>) l).onEvent(event);
                    } catch (Exception e) {
                        LoggerUtil.error("异步事件处理异常: " + event.getClass().getSimpleName(), e);
                    }
                }
            }
        });
    }

    @FunctionalInterface
    public interface EventListener<T> {
        void onEvent(T event);
    }
}
