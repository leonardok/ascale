package me.leok.scaleduino;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import me.leok.scaleduino.fragments.FindScaleFragment;

public class ScaleActivity extends FragmentActivity {

    private BluetoothDevice mDevice;
    private BluetoothSocket mmSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public BluetoothSocket getSocket() {
        return mmSocket;
    }

    public void setSocket(BluetoothSocket mmSocket) {
        this.mmSocket = mmSocket;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public void setOutputStream(OutputStream mOutputStream) {
        this.mOutputStream = mOutputStream;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public void setInputStream(InputStream mInputStream) {
        this.mInputStream = mInputStream;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_scale_fragment);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            FindScaleFragment firstFragment = new FindScaleFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
    }

    public void connect2scale() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mOutputStream = mmSocket.getOutputStream();
        mInputStream = mmSocket.getInputStream();
    }
}
