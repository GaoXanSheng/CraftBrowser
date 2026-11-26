package top.yunmouren.craftbrowser.client.browser.Rpc;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static com.mojang.text2speech.Narrator.LOGGER;

public class RpcClient implements InvocationHandler, AutoCloseable {
    private static final Gson GSON = new Gson();

    private static final String PREFIX_MAP = "NCEF_RPC_";
    private static final String PREFIX_REQ_EVENT = "NCEF_REQ_";
    private static final String PREFIX_EVT_MAP = "NCEF_EVT_";
    private static final String PREFIX_EVT_READY = "NCEF_EVT_READY_";

    private static final int RPC_MAP_SIZE = 1024 * 1024 * 2;
    private static final int EVT_MAP_SIZE = 1024 * 1024;
    private static final int SLOT_SIZE = 65536;
    private static final int SLOTS = 15;
    private static final int HEADER = 8;
    private static final int SLOT_PAYLOAD = SLOT_SIZE - 4;

    private static final int IDLE_WAIT_MS = 200;
    private static final long REPLY_TIMEOUT_MS = 10000;
    private static final long CREATE_BROWSER_TIMEOUT_MS = 120000;

    private static final String REPLY_METHOD = "__REPLY__";
    private static final String ERROR_KEY = "__error__";

    private static final String METHOD_TO_STRING = "toString";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_EQUALS = "equals";
    private static final String PROXY_NAME = "RpcClientProxy";

    private final HANDLE hMap;
    private final HANDLE hEvtMap;
    private final HANDLE hReq;
    private final HANDLE hEvtReady;
    private final Pointer pCmd;
    private final Pointer pEvt;

    private final Object sendLock = new Object();
    private final AtomicLong seq = new AtomicLong();
    private final AtomicLong sendDrops = new AtomicLong();
    private final Map<Long, CompletableFuture<Object>> pending = new ConcurrentHashMap<>();

    private final Thread eventThread;
    private volatile boolean running = true;
    private BiConsumer<String, Object[]> eventHandler;
    private final BrowserEventBus eventBus = new BrowserEventBus();

    public BrowserEventBus getEventBus() {
        return eventBus;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass, String rpcId) {
        try {
            RpcClient client = new RpcClient(rpcId);
            client.setEventHandler(client.eventBus::dispatch);
            return (T) Proxy.newProxyInstance(
                    interfaceClass.getClassLoader(),
                    new Class[]{interfaceClass},
                    client
            );
        } catch (Exception e) {
            throw new RuntimeException("RPC Connect Failed for ID: " + rpcId, e);
        }
    }

    private RpcClient(String rpcId) throws Exception {
        hMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_READ_WRITE, false, PREFIX_MAP + rpcId);
        if (hMap == null) throw new Exception("SharedMemory Not Found: " + PREFIX_MAP + rpcId);

        pCmd = Win32Native.INSTANCE.MapViewOfFile(hMap, Win32Native.FILE_MAP_READ_WRITE, 0, 0, RPC_MAP_SIZE);
        hReq = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_REQ_EVENT + rpcId);

        hEvtMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_READ_WRITE, false, PREFIX_EVT_MAP + rpcId);
        if (hEvtMap == null) throw new Exception("Event SharedMemory Not Found: " + PREFIX_EVT_MAP + rpcId);

        pEvt = Win32Native.INSTANCE.MapViewOfFile(hEvtMap, Win32Native.FILE_MAP_READ_WRITE, 0, 0, EVT_MAP_SIZE);
        hEvtReady = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_EVT_READY + rpcId);

        eventThread = new Thread(this::eventLoop, "NCEF-Event-Loop-" + rpcId);
        eventThread.setDaemon(true);
        eventThread.start();
    }

    public void setEventHandler(BiConsumer<String, Object[]> handler) {
        this.eventHandler = handler;
    }

    private boolean send(RpcPacket packet) {
        byte[] body = GSON.toJson(packet).getBytes(StandardCharsets.UTF_8);
        if (body.length > SLOT_PAYLOAD) return false;

        synchronized (sendLock) {
            int head = pCmd.getInt(0);
            int tail = pCmd.getInt(4);
            if (head - tail >= SLOTS) return false;

            int off = HEADER + Integer.remainderUnsigned(head, SLOTS) * SLOT_SIZE;
            pCmd.setInt(off, body.length);
            pCmd.write(off + 4, body, 0, body.length);
            pCmd.setInt(0, head + 1);
        }

        Win32Native.INSTANCE.SetEvent(hReq);
        return true;
    }

    private void eventLoop() {
        while (running) {
            int status = Win32Native.INSTANCE.WaitForSingleObject(hEvtReady, IDLE_WAIT_MS);
            if (status != Win32Native.WAIT_OBJECT_0) continue;
            try {
                drainEvents();
            } catch (Throwable t) {
                LOGGER.error("[RpcClient] Event Loop Error: {}", t.getMessage());
            }
        }
    }

    private void drainEvents() {
        int head = pEvt.getInt(0);
        int tail = pEvt.getInt(4);

        while (tail != head) {
            int off = HEADER + Integer.remainderUnsigned(tail, SLOTS) * SLOT_SIZE;
            int len = pEvt.getInt(off);
            if (len <= 0 || len > SLOT_PAYLOAD) {
                tail = head;
                pEvt.setInt(4, tail);
                return;
            }

            String json = new String(pEvt.getByteArray(off + 4, len), StandardCharsets.UTF_8);
            tail++;
            pEvt.setInt(4, tail);

            dispatch(json);
            head = pEvt.getInt(0);
        }
    }

    private void dispatch(String json) {
        RpcPacket packet;
        try {
            packet = GSON.fromJson(json, RpcPacket.class);
        } catch (Exception e) {
            return;
        }
        if (packet == null) return;

        if (REPLY_METHOD.equals(packet.Method) && packet.Args != null && packet.Args.length >= 2) {
            long id = ((Number) packet.Args[0]).longValue();
            CompletableFuture<Object> future = pending.remove(id);
            if (future != null) future.complete(packet.Args[1]);
            return;
        }

        if (eventHandler != null) eventHandler.accept(packet.Method, packet.Args);
    }

    private static long timeoutFor(String methodName) {
        return "CreateBrowser".equals(methodName) ? CREATE_BROWSER_TIMEOUT_MS : REPLY_TIMEOUT_MS;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();

        if (methodName.equals(METHOD_TO_STRING)) return PROXY_NAME;
        if (methodName.equals(METHOD_HASH_CODE)) return System.identityHashCode(proxy);
        if (methodName.equals(METHOD_EQUALS)) return proxy == args[0];

        if (method.getReturnType() == Void.TYPE) {
            if (!send(new RpcPacket(methodName, args, 0L))) {
                long dropped = sendDrops.incrementAndGet();
                if (dropped % 100 == 1) LOGGER.warn("[RpcClient] Command ring full, dropped {} (last: {})", dropped, methodName);
            }
            return null;
        }

        long id = seq.incrementAndGet();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pending.put(id, future);

        if (!send(new RpcPacket(methodName, args, id))) {
            pending.remove(id);
            throw new IllegalStateException("RPC ring full: " + methodName);
        }

        try {
            Object result = future.get(timeoutFor(methodName), TimeUnit.MILLISECONDS);
            if (result instanceof Map && ((Map<?, ?>) result).containsKey(ERROR_KEY)) {
                throw new IllegalStateException("RPC error [" + methodName + "]: " + ((Map<?, ?>) result).get(ERROR_KEY));
            }
            return result == null ? null : GSON.fromJson(GSON.toJsonTree(result), method.getReturnType());
        } catch (TimeoutException e) {
            throw new IllegalStateException("RPC timeout after " + timeoutFor(methodName) + "ms: " + methodName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("RPC interrupted: " + methodName);
        } catch (ExecutionException e) {
            throw new IllegalStateException("RPC failed: " + methodName, e.getCause());
        } finally {
            pending.remove(id);
        }
    }

    @Override
    public void close() {
        running = false;
        if (eventThread != null) {
            try {
                eventThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (CompletableFuture<Object> future : pending.values()) {
            future.completeExceptionally(new IllegalStateException("RPC closed"));
        }
        pending.clear();

        Win32Native.INSTANCE.UnmapViewOfFile(pCmd);
        Win32Native.INSTANCE.CloseHandle(hMap);
        Win32Native.INSTANCE.CloseHandle(hReq);

        Win32Native.INSTANCE.UnmapViewOfFile(pEvt);
        Win32Native.INSTANCE.CloseHandle(hEvtMap);
        Win32Native.INSTANCE.CloseHandle(hEvtReady);
    }

    private static class RpcPacket {
        String Method;
        Object[] Args;
        long Id;

        RpcPacket(String method, Object[] args, long id) {
            this.Method = method;
            this.Args = args;
            this.Id = id;
        }
    }
}
