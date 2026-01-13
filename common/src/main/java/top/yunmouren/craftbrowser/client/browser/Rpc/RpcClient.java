package top.yunmouren.craftbrowser.client.browser.Rpc;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class RpcClient implements InvocationHandler, AutoCloseable {
    private static final Gson gson = new Gson();
    private final HANDLE hMap, hReq, hRes;
    private final Pointer pBase;
    private static final int REQ_OFFSET = 0;
    private static final int RES_OFFSET = 1024 * 512;
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
            return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, client);
        } catch (Exception e) {
            throw new RuntimeException("RPC Connect Failed", e);
        }
    }

    private RpcClient(String rpcId) throws Exception {
        String mapName = "NCEF_RPC_" + rpcId;
        String reqName = "NCEF_REQ_" + rpcId;
        String resName = "NCEF_RES_" + rpcId;

        hMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_ALL_ACCESS, false, mapName);
        if (hMap == null) throw new Exception("SharedMemory Not Found: " + mapName);

        pBase = Win32Native.INSTANCE.MapViewOfFile(hMap, Win32Native.FILE_MAP_ALL_ACCESS, 0, 0, 1024 * 1024);
        hReq = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, reqName);
        hRes = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, resName);

        String evtMapName = "NCEF_EVT_" + rpcId;
        String evtReadyName = "NCEF_EVT_READY_" + rpcId;
        String evtAckName = "NCEF_EVT_ACK_" + rpcId;

        hEvtMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_ALL_ACCESS, false, evtMapName);
        if (hEvtMap == null) throw new Exception("Event SharedMemory Not Found: " + evtMapName);

        pEvtBase = Win32Native.INSTANCE.MapViewOfFile(hEvtMap, Win32Native.FILE_MAP_ALL_ACCESS, 0, 0, 512 * 1024);
        hEvtReady = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, evtReadyName);
        hEvtAck = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, evtAckName);

        eventThread = new Thread(this::eventLoop, "NCEF-Event-Loop-" + rpcId);
        eventThread.setDaemon(true);
        eventThread.start();
    }

    public void setEventHandler(BiConsumer<String, Object[]> handler) {
        this.eventHandler = handler;
    }

    private void eventLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            int status = Win32Native.INSTANCE.WaitForSingleObject(hEvtReady, 5000);

            if (status == Win32Native.WAIT_OBJECT_0) {
                try {
                    int len = pEvtBase.getInt(0);
                    if (len > 0) {
                        byte[] data = pEvtBase.getByteArray(4, len);
                        String json = new String(data, StandardCharsets.UTF_8);
                        RpcPacket packet = gson.fromJson(json, RpcPacket.class);
                        if (eventHandler != null && packet != null) {
                            eventHandler.accept(packet.Method, packet.Args);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[RpcClient] Event Error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    Win32Native.INSTANCE.SetEvent(hEvtAck);
                }
            }
        }
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString")) return "RpcClientProxy";
        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
        if (method.getName().equals("equals")) return proxy == args[0];

        RpcPacket packet = new RpcPacket(method.getName(), args);
        byte[] reqData = gson.toJson(packet).getBytes(StandardCharsets.UTF_8);
        pBase.setInt(REQ_OFFSET, reqData.length);
        pBase.write(REQ_OFFSET + 4, reqData, 0, reqData.length);
        Win32Native.INSTANCE.SetEvent(hReq);
        int wait = Win32Native.INSTANCE.WaitForSingleObject(hRes, 5000);
        if (wait != 0) {
            throw new RuntimeException("RPC CallTimeout: " + method.getName());
        }
        int resLen = pBase.getInt(RES_OFFSET);
        if (resLen <= 0) return null;

        byte[] resData = pBase.getByteArray(RES_OFFSET + 4, resLen);
        String resJson = new String(resData, StandardCharsets.UTF_8);

        if (method.getReturnType().equals(Void.TYPE)) return null;
        return gson.fromJson(resJson, method.getReturnType());
    }

    @Override
    public void close() {
        running = false;
        try {
            if (eventThread != null) eventThread.interrupt();
        } catch (Exception ignored) {}

        if (pBase != null) Win32Native.INSTANCE.UnmapViewOfFile(pBase);
        if (hMap != null) Win32Native.INSTANCE.CloseHandle(hMap);
        if (hReq != null) Win32Native.INSTANCE.CloseHandle(hReq);
        if (hRes != null) Win32Native.INSTANCE.CloseHandle(hRes);
        if (pEvtBase != null) Win32Native.INSTANCE.UnmapViewOfFile(pEvtBase);
        if (hEvtMap != null) Win32Native.INSTANCE.CloseHandle(hEvtMap);
        if (hEvtReady != null) Win32Native.INSTANCE.CloseHandle(hEvtReady);
        if (hEvtAck != null) Win32Native.INSTANCE.CloseHandle(hEvtAck);
    }

    private static class RpcPacket {
        String Method;
        Object[] Args;

        RpcPacket(String m, Object[] a) {
            Method = m;
            Args = a;
        }
    }
}