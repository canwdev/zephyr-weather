package com.canwdev.zephyr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TextView;

import com.canwdev.zephyr.util.Conf;

import org.w3c.dom.Text;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView versionText = (TextView) findViewById(R.id.textView_version);

        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
            int versionCode = this.getPackageManager().getPackageInfo(pkName, 0).versionCode;

            versionText.setText(pkName+"\n"+versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        CardView cardView = (CardView) findViewById(R.id.card_about);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_card_show);
        cardView.startAnimation(animation);
    }

    public void card_goGithub(View view) {
        Intent iGoGithub = new Intent(Intent.ACTION_VIEW);
        iGoGithub.setData(Uri.parse(Conf.GITHUB_ADDRESS));
        startActivity(iGoGithub);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
