package com.canwdev.zephyr.util;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.canwdev.zephyr.R;
import com.canwdev.zephyr.gson.Weather;
import com.canwdev.zephyr.util.Utility;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "Test!!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void onButtonClick(View view) {
        Utility util = new Utility(this);
        Weather weather = util.getWeather();
        Log.d(TAG, "onButtonClick: "+weather.status);
    }
}
