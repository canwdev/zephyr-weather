package com.canwdev.zephyr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;

import com.canwdev.zephyr.util.Conf;

public class SettingsActivity extends AppCompatActivity {

    Switch serviceSwitch;
    Switch backgroundSwitch;
    private boolean doNotSave = false;

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

        // 打开时的动画
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_settings);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_card_show);
        scrollView.startAnimation(animation);

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

    public void button_setArea(View view) {
        Intent intent = new Intent(this, ChooseAreaActivity.class);
        startActivity(intent);
    }

    public void button_customArea(View view) {
        final EditText editText = new EditText(this);
        editText.setText(pref.getString(Conf.PREF_WEATHER_ID, null));
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.settings_custom_area_code))
                .setView(editText)
                .setNeutralButton("LIST", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(Conf.HEWEATHER_CITY_LIST));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel , null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String newWeatherId = editText.getText().toString();
                        if (!newWeatherId.isEmpty()) {
                            editor.putString(Conf.PREF_WEATHER_ID, newWeatherId);
                            editor.apply();
                        }
                    }
                });
        dialog.show();
    }

    public void button_customKey(View view) {
        final EditText editText = new EditText(this);
        editText.setHint("<API Key>");
        editText.setText(pref.getString(Conf.PREF_API_KEY, null));
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.settings_custom_api_key))
                .setView(editText)
                .setNeutralButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(Conf.HEWEATHER_REGISTER));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putString(Conf.PREF_API_KEY, null);
                        editor.apply();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String newApiKey = editText.getText().toString();
                        if (!newApiKey.isEmpty()) {
                            editor.putString(Conf.PREF_API_KEY, newApiKey);
                            editor.apply();
                        }
                    }
                });
        dialog.show();
    }

    public void button_clearAllSettings(View view) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.settings_clear_all)+"?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.clear();
                        editor.apply();
                        doNotSave = true;
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (doNotSave) {

        } else {
            saveSettings();
        }
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
