package com.canwdev.zephyr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.canwdev.zephyr.db.RecentArea;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.Utility;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentAreaActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<RecentArea> recentAreaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_area);

        listView = (ListView) findViewById(R.id.ListView_recentArea);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String areaName = recentAreaList.get(i).getAreaName();
                String weatherId = recentAreaList.get(i).getWeatherId();
                // 保存设置
                SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                editor.putString(Conf.PREF_AREA_NAME, areaName);
                editor.putString(Conf.PREF_WEATHER_ID, weatherId);
                editor.apply();

                Intent intent = new Intent();
                intent.putExtra("city_weather_id", weatherId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        queryRecentArea();
    }

    private void queryRecentArea() {
        recentAreaList = DataSupport.findAll(RecentArea.class);
        if (recentAreaList.size() > 0) {
            dataList.clear();
            // 倒序输出
            for (RecentArea recentArea: recentAreaList) {
                dataList.add(0, recentArea.getAreaName()+"  [" + recentArea.getWeatherId() + "]");
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
            }
            Collections.reverse(recentAreaList);

        } else {
            Toast.makeText(this, getResources().getString(R.string.no_recent_area), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO 更新小部件

    }
}
