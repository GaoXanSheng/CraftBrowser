package top.yunmouren.craftbrowser.client.browser.Rpc;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class RpcClient implements InvocationHandler, AutoCloseable {
    private static final Gson GSON = new Gson();

    private static final String PREFIX_MAP = "NCEF_RPC_";
    private static final String PREFIX_REQ_EVENT = "NCEF_REQ_";
    private static final String PREFIX_RES_EVENT = "NCEF_RES_";
    private static final String PREFIX_EVT_MAP = "NCEF_EVT_";
    private static final String PREFIX_EVT_READY = "NCEF_EVT_READY_";
    private static final String PREFIX_EVT_ACK = "NCEF_EVT_ACK_";

    private static final int RPC_MAP_SIZE = 1024 * 1024 * 8;
    private static final int EVENT_MAP_SIZE = RPC_MAP_SIZE/2;
    private static final int REQ_OFFSET = 0;
    private static final int RES_OFFSET = RPC_MAP_SIZE/2;
    private static final int RPC_TIMEOUT_MS = 10000;

    private static final String METHOD_TO_STRING = "toString";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_EQUALS = "equals";
    private static final String PROXY_NAME = "RpcClientProxy";

    private final HANDLE hMap, hReq, hRes;
    private final Pointer pBase;
    private final HANDLE hEvtMap, hEvtReady, hEvtAck;
    private final Pointer pEvtBase;

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
        // 初始化 RPC 通道
        hMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_READ_WRITE, false, PREFIX_MAP + rpcId);
        if (hMap == null) throw new Exception("SharedMemory Not Found: " + PREFIX_MAP + rpcId);

        pBase = Win32Native.INSTANCE.MapViewOfFile(hMap, Win32Native.FILE_MAP_READ_WRITE, 0, 0, RPC_MAP_SIZE);
        hReq = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_REQ_EVENT + rpcId);
        hRes = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_RES_EVENT + rpcId);

        // 初始化 Event 通道
        hEvtMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_READ_WRITE, false, PREFIX_EVT_MAP + rpcId);
        if (hEvtMap == null) throw new Exception("Event SharedMemory Not Found: " + PREFIX_EVT_MAP + rpcId);

        pEvtBase = Win32Native.INSTANCE.MapViewOfFile(hEvtMap, Win32Native.FILE_MAP_READ_WRITE, 0, 0, EVENT_MAP_SIZE);
        hEvtReady = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_EVT_READY + rpcId);
        hEvtAck = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, PREFIX_EVT_ACK + rpcId);

        eventThread = new Thread(this::eventLoop, "NCEF-Event-Loop-" + rpcId);
        eventThread.setDaemon(true);
        eventThread.start();
    }

    public void setEventHandler(BiConsumer<String, Object[]> handler) {
        this.eventHandler = handler;
    }

    private void eventLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            int status = Win32Native.INSTANCE.WaitForSingleObject(hEvtReady, RPC_TIMEOUT_MS);

            if (status == Win32Native.WAIT_OBJECT_0) {
                try {
                    int len = pEvtBase.getInt(0);
                    if (len > 0) {
                        byte[] data = pEvtBase.getByteArray(4, len);
                        String json = new String(data, StandardCharsets.UTF_8);
                        RpcPacket packet = GSON.fromJson(json, RpcPacket.class);
                        if (eventHandler != null && packet != null) {
                            eventHandler.accept(packet.Method, packet.Args);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[RpcClient] Event Loop Error: " + e.getMessage());
                } finally {
                    Win32Native.INSTANCE.SetEvent(hEvtAck);
                }
            }
        }
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();

        if (methodName.equals(METHOD_TO_STRING)) return PROXY_NAME;
        if (methodName.equals(METHOD_HASH_CODE)) return System.identityHashCode(proxy);
        if (methodName.equals(METHOD_EQUALS)) return proxy == args[0];

        RpcPacket packet = new RpcPacket(methodName, args);
        byte[] reqData = GSON.toJson(packet).getBytes(StandardCharsets.UTF_8);

        pBase.setInt(REQ_OFFSET, reqData.length);
        pBase.write(REQ_OFFSET + 4, reqData, 0, reqData.length);

        Win32Native.INSTANCE.SetEvent(hReq);

        int wait = Win32Native.INSTANCE.WaitForSingleObject(hRes, RPC_TIMEOUT_MS);
        if (wait != Win32Native.WAIT_OBJECT_0) {
            throw new RuntimeException("RPC Call Timeout: " + methodName);
        }

        int resLen = pBase.getInt(RES_OFFSET);
        if (resLen <= 0) return null;

        byte[] resData = pBase.getByteArray(RES_OFFSET + 4, resLen);
        String resJson = new String(resData, StandardCharsets.UTF_8);

        if (method.getReturnType().equals(Void.TYPE)) return null;
        return GSON.fromJson(resJson, method.getReturnType());
    }

    @Override
    public void close() {
        running = false;
        if (eventThread != null) eventThread.interrupt();

        Win32Native.INSTANCE.UnmapViewOfFile(pBase);
        Win32Native.INSTANCE.CloseHandle(hMap);
        Win32Native.INSTANCE.CloseHandle(hReq);
        Win32Native.INSTANCE.CloseHandle(hRes);

        Win32Native.INSTANCE.UnmapViewOfFile(pEvtBase);
        Win32Native.INSTANCE.CloseHandle(hEvtMap);
        Win32Native.INSTANCE.CloseHandle(hEvtReady);
        Win32Native.INSTANCE.CloseHandle(hEvtAck);
    }

    private static class RpcPacket {
        String Method;
        Object[] Args;

        RpcPacket(String m, Object[] a) {
            this.Method = m;
            this.Args = a;
        }
    }
}