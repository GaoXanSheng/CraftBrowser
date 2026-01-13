package top.yunmouren.craftbrowser.client.browser.rpc;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

/**
 * win32 magic
 */
public class RpcClient implements InvocationHandler, AutoCloseable {
    private static final Gson gson = new Gson();
    private final HANDLE hMap, hReq, hRes;
    private final Pointer pBase;

    private static final int REQ_OFFSET = 0;
    private static final int RES_OFFSET = 512 * 1024; // 512KB 偏移

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass, String rpcId) {
        try {
            RpcClient client = new RpcClient(rpcId);
            return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, client);
        } catch (Exception e) {
            throw new RuntimeException("unableToConnectTo NCEF RPC: " + rpcId, e);
        }
    }

    private RpcClient(String rpcId) throws Exception {
        String mapName = "NCEF_RPC_" + rpcId;
        String reqName = "NCEF_REQ_" + rpcId;
        String resName = "NCEF_RES_" + rpcId;

        hMap = Win32Native.INSTANCE.OpenFileMapping(Win32Native.FILE_MAP_ALL_ACCESS, false, mapName);
        if (hMap == null) throw new Exception("sharedMemoryNotFound: " + mapName);

        pBase = Win32Native.INSTANCE.MapViewOfFile(hMap, Win32Native.FILE_MAP_ALL_ACCESS, 0, 0, 1024 * 1024);
        hReq = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, reqName);
        hRes = Win32Native.INSTANCE.OpenEvent(Win32Native.EVENT_ALL_ACCESS, false, resName);
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) {
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
        Win32Native.INSTANCE.UnmapViewOfFile(pBase);
        Win32Native.INSTANCE.CloseHandle(hMap);
        Win32Native.INSTANCE.CloseHandle(hReq);
        Win32Native.INSTANCE.CloseHandle(hRes);
    }

    private static class RpcPacket {
        String Method; Object[] Args;
        RpcPacket(String m, Object[] a) { Method = m; Args = a; }
    }
}