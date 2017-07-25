package me.leok.scaleduino.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;

import me.leok.scaleduino.R;
import me.leok.scaleduino.ScaleActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectToScaleFragment extends Fragment {


    private static final String TAG = "ConnectToScaleFragment";

    public ConnectToScaleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect_to_scale, container, false);


        final ScaleActivity thisActivity = (ScaleActivity) getActivity();


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {
                // something you know that will take a few seconds

                try {
                    thisActivity.scale.connect();
                } catch (IOException e) {
                    Log.v(TAG, "Failed to connect to scale! Reason: " + e.toString());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Failed to connect to scale. Make sure you selected a valid scale.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    toPrev();
                    return null;
                }

                toNext();
                return null;
            }
        }.execute();


        return view;
    }

    private void toPrev() {
        getFragmentManager().popBackStack();
    }

    private void toNext() {
        Fragment weightFragment = new WeightFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.fragment_container, weightFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }


}
