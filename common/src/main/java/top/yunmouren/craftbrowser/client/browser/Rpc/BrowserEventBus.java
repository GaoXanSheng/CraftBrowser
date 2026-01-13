package top.yunmouren.craftbrowser.client.browser.Rpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;
import top.yunmouren.craftbrowser.client.browser.Rpc.annotation.BrowserRpcEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserEventBus {
    private static final Gson gson = new Gson();
    private final Map<String, List<HandlerWrapper>> subscribers = new ConcurrentHashMap<>();
    private IBrowserController responseChannel;
    public void setResponseChannel(IBrowserController controller) {
        this.responseChannel = controller;
    }
    public void register(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BrowserRpcEvent.class)) {
                BrowserRpcEvent annotation = method.getAnnotation(BrowserRpcEvent.class);
                String eventName = annotation.value().isEmpty() ? method.getName() : annotation.value();
                method.setAccessible(true);
                subscribers.computeIfAbsent(eventName, k -> new ArrayList<>())
                        .add(new HandlerWrapper(instance, method, annotation.runOnMainThread()));
            }
        }
    }
    public void dispatch(String eventName, Object[] rawArgs) {
        if ("__RPC_CALL__".equals(eventName)) {
            handleRpcCall(rawArgs);
            return;
        }
        List<HandlerWrapper> handlers = subscribers.get(eventName);
        if (handlers == null || handlers.isEmpty()) return;

        for (HandlerWrapper handler : handlers) {
            Runnable task = () -> {
                try {
                    Object[] convertedArgs = convertArgs(handler.method, rawArgs);
                    handler.method.invoke(handler.instance, convertedArgs);
                } catch (Exception e) {
                    System.err.println("[BrowserRPC] Error invoking event: " + eventName);
                    e.printStackTrace();
                }
            };

            if (handler.runOnMainThread) {
                Minecraft.getInstance().execute(task);
            } else {
                task.run();
            }
        }
    }
    private Object[] convertArgs(Method method, Object[] rawArgs) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (rawArgs == null || rawArgs.length == 0) return new Object[0];
        int len = Math.min(paramTypes.length, rawArgs.length);
        Object[] finalArgs = new Object[len];

        for (int i = 0; i < len; i++) {
            Object arg = rawArgs[i];
            Class<?> targetType = paramTypes[i];
            JsonElement jsonElement = gson.toJsonTree(arg);
            finalArgs[i] = gson.fromJson(jsonElement, targetType);
        }
        return finalArgs;
    }

    private static record HandlerWrapper(Object instance, Method method, boolean runOnMainThread) {}
    private void handleRpcCall(Object[] args) {
        if (args.length < 2) return;
        String reqId = (String) args[0];
        String targetMethod = (String) args[1];
        Object[] realArgs = new Object[args.length - 2];
        System.arraycopy(args, 2, realArgs, 0, realArgs.length);
        List<HandlerWrapper> handlers = subscribers.get(targetMethod);
        if (handlers != null && !handlers.isEmpty()) {
            HandlerWrapper handler = handlers.get(0);
            Minecraft.getInstance().execute(() -> {
                try {
                    Object[] convertedArgs = convertArgs(handler.method, realArgs);
                    Object result = handler.method.invoke(handler.instance, convertedArgs);
                    if (responseChannel != null) {
                        responseChannel.ResolveJsPromise(reqId, result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (responseChannel != null) {
                        responseChannel.ResolveJsPromise(reqId, "JAVA_ERROR: " + e.getMessage());
                    }
                }
            });
        }
    }
}