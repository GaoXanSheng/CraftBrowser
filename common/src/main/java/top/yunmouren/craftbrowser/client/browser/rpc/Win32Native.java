package top.yunmouren.craftbrowser.client.browser.rpc;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * JNA
 */
public interface Win32Native extends StdCallLibrary {
    Win32Native INSTANCE = Native.load("kernel32", Win32Native.class, W32APIOptions.DEFAULT_OPTIONS);
    int FILE_MAP_ALL_ACCESS = 0xF001F;
    int EVENT_ALL_ACCESS = 0x1F0003;

    // 句柄操作
    WinNT.HANDLE OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName);
    Pointer MapViewOfFile(WinNT.HANDLE hFileMappingObject, int dwDesiredAccess, int dwFileOffsetHigh, int dwFileOffsetLow, int dwNumberOfBytesToMap);
    boolean UnmapViewOfFile(Pointer lpBaseAddress);
    boolean CloseHandle(WinNT.HANDLE hObject);

    // 事件操作
    WinNT.HANDLE OpenEvent(int dwDesiredAccess, boolean bInheritHandle, String lpName);
    boolean SetEvent(WinNT.HANDLE hEvent);
    int WaitForSingleObject(WinNT.HANDLE hHandle, int dwMilliseconds);
}