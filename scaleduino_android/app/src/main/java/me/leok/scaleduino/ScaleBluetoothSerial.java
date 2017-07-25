package me.leok.scaleduino;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by leonardo on 7/25/17.
 */


public class ScaleBluetoothSerial {
    BluetoothDevice mDevice;
    BluetoothSocket mSocket;
    OutputStream mOutputStream;
    InputStream mInputStream;

    private String TAG = "ScaleBluetoothSerial";
    private boolean stopWorker;
    private int readBufferPosition;
    private byte[] readBuffer;
    private OnDataReceivedListener mOnDataReceivedListener;

    public interface OnDataReceivedListener {
        void onDataReceived(String data);
    }

    public void setmOnDataReceivedListener(OnDataReceivedListener mOnDataReceivedListener) {
        this.mOnDataReceivedListener = mOnDataReceivedListener;
    }


    public ScaleBluetoothSerial(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public void connect() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        mSocket.connect();
        mOutputStream = mSocket.getOutputStream();
        mInputStream = mSocket.getInputStream();
    }

    public void tare() throws IOException {
        Log.w(TAG, "Taring scale");
        mOutputStream.write("t".getBytes());
        mOutputStream.flush();
    }

    public void listen() {
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        final byte delimiter = 10; //This is the ASCII code for a newline character

        Thread workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        // check if there are data available
                        int bytesAvailable = mInputStream.available();
                        if (bytesAvailable <= 0) continue;

                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];

                            if (b != delimiter) {
                                readBuffer[readBufferPosition++] = b;
                            }

                            // build the string
                            else {
                                String receivedData = new String(readBuffer, 0, readBufferPosition, "US-ASCII");
                                readBufferPosition = 0;

                                if (mOnDataReceivedListener != null)
                                    mOnDataReceivedListener.onDataReceived(receivedData);
                            }
                        }

                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
}
