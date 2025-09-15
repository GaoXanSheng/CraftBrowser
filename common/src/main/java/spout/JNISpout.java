package spout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JNISpout {
    public JNISpout(){
        DllInit();
    }

    protected static void DllInit() {
        {
            String sunDataModel = System.getProperty("sun.arch.data.model");
            String dllName = sunDataModel.equals("32") ? "JNISpout_32.dll" : "JNISpout_64.dll";

            try (InputStream dllStream = JNISpout.class.getResourceAsStream("/native/" + dllName)) {
                if (dllStream == null) throw new RuntimeException("Cannot find DLL: " + dllName);

                Path tempDll = Files.createTempFile(dllName.replace(".dll", ""), ".dll");
                tempDll.toFile().deleteOnExit();
                Files.copy(dllStream, tempDll, StandardCopyOption.REPLACE_EXISTING);

                System.load(tempDll.toAbsolutePath().toString());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Spout DLL", e);
            }
        }
    }


    // Initialization - return a pointer to a spout object
    public native long init();

    public native void deInit(long ptr);

    // Sender
    public native boolean createSender(String name, int width, int height, long ptr);

    public native boolean updateSender(String name, int width, int height, long ptr);

    public native boolean releaseSender(long ptr);

    public native boolean sendTexture(int w, int h, int texID, int texTarget, boolean bInvert, long ptr);

    // Receiver
    public native void setReceiverName(String name, long ptr);

    public native boolean createReceiver(String name, int[] dim, long ptr);

    public native boolean releaseReceiver(long ptr);

    public native boolean receivePixels(int[] dim, int[] pix, long ptr);

    public native boolean receiveTexture(int[] dim, int texID, int texTarget, boolean bInvert, long ptr);

    public native boolean senderDialog(long ptr);

    public native String getSenderName(long ptr);

    public native int getSenderWidth(long ptr);

    public native int getSenderHeight(long ptr);

    public native int getSenderFormat(long ptr);

    public native String getSenderFormatName(long ptr);

    // Frame count
    public native float getSenderFps(long ptr);

    public native int getSenderFrame(long ptr);

    public native boolean isFrameNew(long ptr);

    public native void disableFrameCount(long ptr);

    // Logging
    public native void enableSpoutLog(long ptr);

    public native void spoutLogToFile(String filename, boolean bAppend, long ptr);

    public native void spoutLogLevel(int level, long ptr);

    public native void spoutLog(String logtext, long ptr);

    public native void spoutLogVerbose(String logtext, long ptr);

    public native void spoutLogNotice(String logtext, long ptr);

    public native void spoutLogWarning(String logtext, long ptr);

    public native void spoutLogError(String logtext, long ptr);

    public native void spoutLogFatal(String logtext, long ptr);

    // MessageBox
    public native int spoutMessageBox(String message, String caption, int type, String instruction, int timeout, long ptr);

    public native void spoutMessageBoxIcon(String iconfile, long ptr);

    public native void spoutMessageBoxButton(int ID, String title, long ptr);

    public native void spoutMessageBoxModeless(boolean bMode, long ptr);

    public native String spoutEditBox(String message, String caption, String text, long ptr);

    public native int spoutComboBox(String message, String caption, String[] items, long ptr);

    public native void spoutMessageBoxWindow(boolean bEnable, long ptr);

    // Utility
    public native boolean copyToClipBoard(String text, long ptr);

    // Common
    public native int getTextureID(long ptr);

    public native boolean getMemoryShareMode(long ptr);

    public native int getShareMode(long ptr);

    // SpoutControls
    public native boolean createControl(String name, String type, float minimum, float maximum, float value, String text, long ptr);

    public native boolean openControls(String name, long ptr);

    public native int checkControls(String[] name, int[] type, float[] value, String[] text, long ptr);

    public native boolean openController(String path, long ptr);

    public native boolean closeControls(long ptr);

    // Shared memory
    public native boolean createSenderMemory(String name, int width, int height, long ptr);

    public native boolean updateSenderMemorySize(String name, int width, int height, long ptr);

    public native boolean writeSenderString(String buf, long ptr);

    public native void closeSenderMemory(long ptr);

    public native long lockSenderMemory(long ptr);

    public native void unlockSenderMemory(long ptr);

    // Sync event signals
    public native void setFrameSync(String SenderName, long ptr);

    public native boolean waitFrameSync(String SenderName, int dwTimeout, long ptr);

    // Per-frame metadata
    public native boolean writeMemoryBuffer(String name, String data, int length, long ptr);

    public native String readMemoryBuffer(String name, int maxlength, long ptr);

    public native boolean createMemoryBuffer(String name, int length, long ptr);

    public native boolean deleteMemoryBuffer(long ptr);

    public native int getMemoryBufferSize(String name, long ptr);

}
