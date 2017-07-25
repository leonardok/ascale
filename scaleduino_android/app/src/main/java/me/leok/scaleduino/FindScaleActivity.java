package me.leok.scaleduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import me.leok.scaleduino.adapters.BluetoothDevicesAdapter;

public class FindScaleActivity extends AppCompatActivity {

    private static final String TAG = "FindScaleActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private ListView scaleListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    String deviceName = "ascale";
    ArrayList<BluetoothDevice> scalesList;
    private BluetoothDevicesAdapter mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_scale);

        scaleListView = (ListView) findViewById(R.id.scaleListView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);


        /*
         * Setup the refresh listener to look for scales when refesh is called
         */
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshDeviceList();
                    }
                });

        turnonBluetooth();
        setScaleListViewAdapter();
        refreshDeviceList();
    }

    /**
     * Set the view adapter
     */
    private void setScaleListViewAdapter() {
        /*
         * Instanciating an array list (you don't need to do this,
         * you already have yours).
         */
        scalesList = new ArrayList<BluetoothDevice>();

        /*
         * This is the array adapter, it takes the context of the activity as a
         * first parameter, the type of list view as a second parameter and your
         * array as a third parameter.
         */
        mArrayAdapter = new BluetoothDevicesAdapter(this, scalesList);

        scaleListView.setAdapter(mArrayAdapter);

        /*
         * set the row click listener
         */
        scaleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "pressed row " + position);

                BluetoothDevice device = (BluetoothDevice) parent.getAdapter().getItem(position);

                Intent intent = new Intent(FindScaleActivity.this, WeightActivity.class);
                intent.putExtra("EXTRA_DEVICE_ADDRESS", device.getAddress());

                startActivity(intent);
                finish();
            }
        });
    }


    /**
     * Check if bluetooth is turned on
     */
    private void turnonBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.v(TAG, "No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }


    /**
     * Find the scale
     */
    private void refreshDeviceList() {
        mSwipeRefreshLayout.setRefreshing(true);

        // create the async task

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

                for (BluetoothDevice device : devices) {
//                    devicesArrayList.add(device);

                    Log.v(TAG, "Found device: " + device.getUuids().length);

                    /*
                    if (deviceName.equals(device.getName())) {
                        mDevice = device;
                        break;
                    }
                    */
                }

                FindScaleActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mArrayAdapter.clear();
                        mArrayAdapter.addAll(devices);
                        mArrayAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });

                Log.v(TAG, "Ended device scan");
                return null;
            }
        }.execute();

    }
}
