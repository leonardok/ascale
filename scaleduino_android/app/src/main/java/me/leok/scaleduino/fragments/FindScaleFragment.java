package me.leok.scaleduino.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import me.leok.scaleduino.ScaleActivity;
import me.leok.scaleduino.R;
import me.leok.scaleduino.adapters.BluetoothDevicesAdapter;


public class FindScaleFragment extends Fragment {
    private static final String TAG = "FindScaleFragment";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private ListView scaleListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    String deviceName = "ascale";
    ArrayList<BluetoothDevice> scalesList;
    private BluetoothDevicesAdapter mArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_scale, container, false);

        scaleListView = (ListView) view.findViewById(R.id.scaleListView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);


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

        // Inflate the layout for this fragment
        return view;
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
        mArrayAdapter = new BluetoothDevicesAdapter(this.getContext(), scalesList);

        scaleListView.setAdapter(mArrayAdapter);

        /*
         * set the row click listener
         */
        scaleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "pressed row " + position);

                BluetoothDevice device = (BluetoothDevice) parent.getAdapter().getItem(position);

                // set the scale device
                ScaleActivity thisActivity = (ScaleActivity) getActivity();
                thisActivity.setDevice(device);

                /*
                 * Change fragments!!
                 */

                Fragment connectFragment = new ConnectToScaleFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.fragment_container, connectFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
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
                    Log.v(TAG, "Found device: " + device.getUuids().length);
                }

                getActivity().runOnUiThread(new Runnable() {
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
