package bk.tung.serial_ver1;

import androidx.appcompat.app.AppCompatActivity;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SerialPort mSerialPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ReceiveThread mReceiveThread;
    private boolean isStart = false;
    Button btn_send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = findViewById(R.id.btn_send);

        openSerialPort();


        btn_send.setOnClickListener(v -> {

            try {
                mOutputStream.write("xin chao".getBytes());

            } catch (IOException e) {
                Log.i(TAG, "Failed to send");
                e.printStackTrace();
            }


        });
        EventBus.getDefault().register(this );
    }

    public void openSerialPort(){
        try {
            //set thong so uart
            mSerialPort = new SerialPort(new File("/dev/ttyHSL0"),115200,0);
            //get  read and write data stream in th serial port
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            isStart = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        getSerialPort();
    }
    //start thread
    private void getSerialPort() {
        if (mReceiveThread == null){
            mReceiveThread = new ReceiveThread();
        }
        mReceiveThread.start();
    }

    //tao thread nhan du lieu tu serial port
    private class ReceiveThread extends Thread{
        @Override
        public void run() {
            super.run();
            //
            while (isStart){
                if (mInputStream == null){
                    return;
                }
                byte[] readData = new byte[1024];
                //doc data input
                try {
                    int size = mInputStream.read(readData);
                    if (size > 0){
                        String readString = ByteArrToHex(readData,0,size);
                        String hexToText = hexToAscii(readString);
//                        Log.i(TAG,"Data Received: " + hexToText);
                        EventBus.getDefault().post(hexToText);
//                        Log.i(TAG,"Data received: "+ Arrays.toString(readString.getBytes()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void closeSerialPort(){
        Log.i(TAG,"Close serial port");
        if (mInputStream != null){
            try {
                mInputStream.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }if (mOutputStream != null){
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isStart = false;
    }

    //ngat bit chan le, so cuoi la 1 le, 0 la chan
    public static int isOdd(int num){ return num & 1;}
    //Hex to int
    public static int HexToInt(String inHex){
        return Integer.parseInt(inHex,16);
    }
    //int to Hex
    public static String IntToHex(int intHex){
        return Integer.toHexString(intHex);
    }
    // Hex to byte
    public static byte HexToByte(String inHex){
        return (byte) Integer.parseInt(inHex, 16);
    }
    // 1byte to 2 hex characters
    public static String Byte2Hex(Byte inByte){
        return String.format("%02x", inByte).toUpperCase();
    }
    //Byte array to hex
    public static String ByteArrToHex(byte[] inByArr){
        StringBuilder stringBuilder = new StringBuilder();
        for (byte valueOf : inByArr){
            stringBuilder.append(Byte2Hex(valueOf));
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
    //convert byte array to hex with parameter
    public static String ByteArrToHex(byte[] inByArr, int offset, int byteCount){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = offset; i < byteCount; i++){
            stringBuilder.append(Byte2Hex(inByArr[i]));
        }
        return stringBuilder.toString();
    }
    // hex to byte array
    public static byte[] HexToByteArr(String inHex){
        byte[] result;
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1){
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex = "0" + inHex;
        }
        else {
            result = new byte[(hexlen/2)];
        }
        int i;
        int j = 0;
        for (i = 0; i < hexlen; i+=2){
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
    public static String hexToAscii(String hexStr){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i +=2){
            String string = hexStr.substring(i, i + 2);
            stringBuilder.append((char)Integer.parseInt(string, 16));
        }
        return stringBuilder.toString();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String string){
        Log.i(TAG,"Eventbus: " + string);
    }
}
