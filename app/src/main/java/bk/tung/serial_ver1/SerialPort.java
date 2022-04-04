package bk.tung.serial_ver1;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort (File device, int baudrate, int flags) throws SecurityException, IOException {
        //check access permssion
        if (!device.canRead() || !device.canWrite()){
            try {
                //missing read/write permission, try to chmod the file
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666" + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()){
                    throw new SecurityException();
                }

            }catch (Exception e){
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null){
            Log.d(TAG, "Native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);

        mFileOutputStream = new FileOutputStream(mFd);
    }

    //getters and setter
    public InputStream getInputStream(){
        return mFileInputStream;
    }

    public OutputStream getOutputStream(){
        return mFileOutputStream;
    }
    //Jni
    private native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    static {
        System.loadLibrary("serial_port");
    }
}
