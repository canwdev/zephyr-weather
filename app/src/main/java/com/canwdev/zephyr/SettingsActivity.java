package com.canwdev.zephyr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;

import com.canwdev.zephyr.util.Conf;

public class SettingsActivity extends AppCompatActivity {

    Switch serviceSwitch;
    Switch backgroundSwitch;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        serviceSwitch = (Switch) findViewById(R.id.switch_enableBackgroundService);
        backgroundSwitch = (Switch) findViewById(R.id.switch_enableBgImage);

        pref = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
        editor = pref.edit();

        loadSettings();
    }

    private void saveSettings() {
        editor.putBoolean(Conf.PREF_ENABLE_SERVICE, serviceSwitch.isChecked());
        editor.putBoolean(Conf.PREF_ENABLE_BG_IMAGE, backgroundSwitch.isChecked());

        editor.apply();
    }

    private void loadSettings() {
        serviceSwitch.setChecked(pref.getBoolean(Conf.PREF_ENABLE_SERVICE, false));
        backgroundSwitch.setChecked(pref.getBoolean(Conf.PREF_ENABLE_BG_IMAGE, true));
    }

    @Override
    protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            saveSettings();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveSettings();
        finish();
    }
}
