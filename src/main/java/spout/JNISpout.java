package spout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JNISpout {
    static {
        DllInit();
    }
    protected static void DllInit(){
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
  public static native long init();
  public static native void deInit(long ptr);
  
  // Sender
  public static native boolean createSender(String name, int width, int height, long ptr);
  public static native boolean updateSender(String name, int width, int height, long ptr);
  public static native boolean releaseSender(long ptr);
  public static native boolean sendTexture(int w, int h, int texID, int texTarget, boolean bInvert, long ptr);

  // Receiver
  public static native void setReceiverName(String name, long ptr);
  public static native boolean createReceiver(String name, int[] dim, long ptr);
  public static native boolean releaseReceiver(long ptr);
  public static native boolean receivePixels(int[] dim, int[] pix, long ptr);
  public static native boolean receiveTexture(int[] dim, int texID, int texTarget, boolean bInvert, long ptr);
  public static native boolean senderDialog(long ptr);
  public static native String getSenderName(long ptr);
  public static native int getSenderWidth(long ptr);
  public static native int getSenderHeight(long ptr);
  public static native int getSenderFormat(long ptr);
  public static native String getSenderFormatName(long ptr);
  
  // Frame count
  public static native float getSenderFps(long ptr);
  public static native int getSenderFrame(long ptr);
  public static native boolean isFrameNew(long ptr);
  public static native void disableFrameCount(long ptr);
  
  // Logging
  public static native void enableSpoutLog(long ptr);
  public static native void spoutLogToFile(String filename, boolean bAppend, long ptr);
  public static native void spoutLogLevel(int level, long ptr);
  public static native void spoutLog(String logtext, long ptr);
  public static native void spoutLogVerbose(String logtext, long ptr);
  public static native void spoutLogNotice(String logtext, long ptr);
  public static native void spoutLogWarning(String logtext, long ptr);
  public static native void spoutLogError(String logtext, long ptr);
  public static native void spoutLogFatal(String logtext, long ptr);
  
  // MessageBox
  public static native int spoutMessageBox(String message, String caption, int type, String instruction, int timeout, long ptr);
  public static native void spoutMessageBoxIcon(String iconfile, long ptr);
  public static native void spoutMessageBoxButton(int ID, String title, long ptr);
  public static native void spoutMessageBoxModeless(boolean bMode, long ptr);
  public static native String spoutEditBox(String message, String caption, String text, long ptr);
  public static native int spoutComboBox(String message, String caption, String[] items, long ptr);
  public static native void spoutMessageBoxWindow(boolean bEnable, long ptr);
  
  // Utility
  public static native boolean copyToClipBoard(String text, long ptr);
    
  // Common
  public static native int getTextureID(long ptr);
  public static native boolean getMemoryShareMode(long ptr);
  public static native int getShareMode(long ptr);
  
  // SpoutControls
  public static native boolean createControl(String name, String type, float minimum, float maximum, float value, String text, long ptr);
  public static native boolean openControls(String name, long ptr);
  public static native int checkControls(String[] name, int[] type, float[] value, String[] text, long ptr);
  public static native boolean openController(String path, long ptr);
  public static native boolean closeControls(long ptr);
  
  // Shared memory
  public static native boolean createSenderMemory(String name, int width, int height, long ptr);
  public static native boolean updateSenderMemorySize(String name, int width, int height, long ptr);
  public static native boolean writeSenderString(String buf, long ptr);
  public static native void closeSenderMemory(long ptr);
  public static native long lockSenderMemory(long ptr);
  public static native void unlockSenderMemory(long ptr);
  
  // Sync event signals
  public static native void setFrameSync(String SenderName, long ptr);
  public static native boolean waitFrameSync(String SenderName, int dwTimeout, long ptr);
  
  // Per-frame metadata
  public static native boolean writeMemoryBuffer(String name, String data, int length, long ptr);
  public static native String readMemoryBuffer(String name, int maxlength, long ptr);
  public static native boolean createMemoryBuffer(String name, int length, long ptr);
  public static native boolean deleteMemoryBuffer(long ptr);
  public static native int getMemoryBufferSize(String name, long ptr);
  
}
