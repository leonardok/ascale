package me.leok.scaleduino.fragments;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import me.leok.scaleduino.R;
import me.leok.scaleduino.ScaleActivity;
import me.leok.scaleduino.ScaleData;
import me.leok.scaleduino.components.PausableChronometer;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeightFragment extends Fragment {

    private static final String TAG = "WeightFragment";

    ScaleActivity mThisActivity;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;


    private long timeWhenStopped = 0;

    TextView title;
    TextView weightText;
    ProgressBar weightProgressBar;
    PausableChronometer chrono;

    Button resetChrono;
    Button startChrono;

    ConstraintLayout loadingLayout;
    ConstraintLayout weightingLayout;

    // Standard SerialPortService ID
    // this uuid identifies the type of service in the target device
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public WeightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_weight, container, false);

        mThisActivity = (ScaleActivity) getActivity();



        weightProgressBar = (ProgressBar) view.findViewById(R.id.weightProgressBar);
        weightText = (TextView) view.findViewById(R.id.weightText);
        title = (TextView) view.findViewById(R.id.title);
        chrono = (PausableChronometer) view.findViewById(R.id.chronometer);
        resetChrono = (Button) view.findViewById(R.id.reset);
        startChrono = (Button) view.findViewById(R.id.start);

        weightingLayout = (ConstraintLayout) view.findViewById(R.id.weightingLayout);
        loadingLayout = (ConstraintLayout) view.findViewById(R.id.loadingLayout);

        weightProgressBar.setScaleY(8f);

        startChrono.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                chrono.start();
            }
        });

        resetChrono.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                chrono.reset();
            }
        });


        /*
         * Starts listening for serial data from BT!
         */
        beginListenForData();


        return view;
    }


    void beginListenForData()
    {
        final Gson gson = new Gson();
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        BluetoothDevice mDevice = mThisActivity.getDevice();
        BluetoothSocket mSocket = mThisActivity.getSocket();
        OutputStream mOutputStream = mThisActivity.getOutputStream();
        final InputStream mInputStream = mThisActivity.getInputStream();

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
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
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

                                                weightText.setText(String.format("%sg", String.format("%.2f", scaleData.weight)));
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
