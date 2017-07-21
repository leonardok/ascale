package me.leok.scaleduino;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.RotateDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";

    Context context;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;




    TextView title;
    TextView weightText;
    ProgressBar weightProgressBar;

    ConstraintLayout loadingLayout;
    ConstraintLayout weightingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        weightProgressBar = (ProgressBar) findViewById(R.id.weightProgressBar);
        weightText = (TextView) findViewById(R.id.weightText);
        title = (TextView) findViewById(R.id.title);

        weightingLayout = (ConstraintLayout) findViewById(R.id.weightingLayout);
        loadingLayout = (ConstraintLayout) findViewById(R.id.loadingLayout);
        weightingLayout.setVisibility(View.INVISIBLE);

        weightProgressBar.setScaleY(8f);


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {
                // something you know that will take a few seconds
                findScale();

                try {
                    connect2scale();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        loadingLayout.setVisibility(View.INVISIBLE);
                        weightingLayout.setVisibility(View.VISIBLE);
                        beginListenForData();
                    }
                });

                return null;
            }
        }.execute();
    }


    private void findScale() {
        String deviceName = "scale";

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null)
        {
            Log.v(TAG, "No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        BluetoothDevice result = null;

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                if (deviceName.equals(device.getName())) {
                    mmDevice = device;
                    break;
                }
            }
        }

        Log.v(TAG, "Found device " + mmDevice);
    }


    private void connect2scale() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
    }


    void beginListenForData()
    {
        final Gson gson = new Gson();
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            // Log.v(TAG, data.toString());
                                            try {
                                                ScaleData scaleData = gson.fromJson(data, ScaleData.class);

                                                weightText.setText(String.format("%sg", String.valueOf((int) scaleData.weight)));
                                                weightProgressBar.setProgress((int) scaleData.weight);
                                            }
                                            catch (Exception e) {}
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
}
