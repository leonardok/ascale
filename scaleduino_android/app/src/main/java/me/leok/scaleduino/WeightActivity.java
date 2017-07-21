package me.leok.scaleduino;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WeightActivity extends AppCompatActivity {

    private Context context;
    private ProgressBar weightProgressBar;
    private TextView weightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        context = WeightActivity.this;

        weightProgressBar = (ProgressBar) findViewById(R.id.weightProgressBar);
        weightText = (TextView) findViewById(R.id.weightText);

        weightProgressBar.setScaleY(3f);
    }
}
