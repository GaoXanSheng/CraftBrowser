package top.yunmouren.craftbrowser.client.browser.Rpc;

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
    int FILE_MAP_READ_WRITE  = 0x0006;
    int EVENT_ALL_ACCESS = 0x1F0003;
    int WAIT_OBJECT_0 = 0x000000;
    WinNT.HANDLE OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName);
    Pointer MapViewOfFile(WinNT.HANDLE hFileMappingObject, int dwDesiredAccess, int dwFileOffsetHigh, int dwFileOffsetLow, int dwNumberOfBytesToMap);
    boolean UnmapViewOfFile(Pointer lpBaseAddress);
    boolean CloseHandle(WinNT.HANDLE hObject);
    WinNT.HANDLE OpenEvent(int dwDesiredAccess, boolean bInheritHandle, String lpName);
    boolean SetEvent(WinNT.HANDLE hEvent);
    int WaitForSingleObject(WinNT.HANDLE hHandle, int dwMilliseconds);
}