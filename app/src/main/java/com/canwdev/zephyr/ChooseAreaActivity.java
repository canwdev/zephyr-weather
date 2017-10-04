package com.canwdev.zephyr;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.canwdev.zephyr.db.City;
import com.canwdev.zephyr.db.County;
import com.canwdev.zephyr.db.Province;
import com.canwdev.zephyr.gson.SearchedArea;
import com.canwdev.zephyr.util.Conf;
import com.canwdev.zephyr.util.HttpUtil;
import com.canwdev.zephyr.util.Utility;
import com.google.gson.Gson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaActivity extends AppCompatActivity {
    public static final String AREA_API_URL = "http://guolin.tech/api/china/";

    // 用于判定当前选择状态的
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    public static final int LEVEL_SEARCH = 3;
    private static final String TAG = "ChooseAreaActivity!!";

    // 各控件
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ImageButton buttonBack;
    private ImageButton buttonSearchArea;
    private ListView listView;

    // 数据
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private SearchedArea searchedArea;
    private String toSearchAreaString;

    // 当前选择状态
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_area);

        titleText = (TextView) findViewById(R.id.textView_fca_title);
        buttonBack = (ImageButton) findViewById(R.id.button_fca_back);
        buttonSearchArea = (ImageButton) findViewById(R.id.button_fca_custom);
        listView = (ListView) findViewById(R.id.listView_fca_area);
        adapter = new ArrayAdapter<String>(this
                , android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        // 点击某一条ListViewItem
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(i).getWeatherId();
                    String areaName = countyList.get(i).getCountyName();
                    // 保存设置
                    SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                    editor.putString(Conf.PREF_AREA_NAME, areaName);
                    editor.putString(Conf.PREF_WEATHER_ID, weatherId);
                    editor.apply();
                    Intent intent = new Intent();
                    intent.putExtra("city_weather_id", weatherId);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (currentLevel == LEVEL_SEARCH) {
                    String weatherId = searchedArea.HeWeather5.get(i).basic.weatherId;
                    String areaName = searchedArea.HeWeather5.get(i).basic.cityName;
                    SharedPreferences.Editor editor = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE).edit();
                    editor.putString(Conf.PREF_AREA_NAME, areaName);
                    editor.putString(Conf.PREF_WEATHER_ID, weatherId);
                    editor.apply();
                    Intent intent = new Intent();
                    intent.putExtra("city_weather_id", weatherId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        // 按钮返回
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                } else if (currentLevel == LEVEL_PROVINCE) {
                    finish();
                } else if (currentLevel == LEVEL_SEARCH) {
                    queryProvinces();
                }
            }
        });
        // 搜索地区
        buttonSearchArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchedArea = null;
                SharedPreferences pref = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                final EditText editText = new EditText(ChooseAreaActivity.this);
                editText.setHint(getString(R.string.search_area_input));
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChooseAreaActivity.this)
                        .setTitle(getString(R.string.search_area))
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                toSearchAreaString = editText.getText().toString();
                                querySearchArea(toSearchAreaString);
                            }
                        });
                dialog.show();
            }
        });

        // 长按输入地区ID
        buttonSearchArea.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SharedPreferences pref = getSharedPreferences(Conf.PREF_FILE_NAME, MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                final EditText editText = new EditText(ChooseAreaActivity.this);
                editText.setText(pref.getString(Conf.PREF_WEATHER_ID, null));
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChooseAreaActivity.this)
                        .setTitle(getString(R.string.settings_custom_area_code))
                        .setView(editText)
                        .setNeutralButton(getResources().getString(R.string.area_list_code), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(Conf.HEWEATHER_CITY_LIST));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final String weatherId = editText.getText().toString();
                                if (!weatherId.isEmpty()) {
                                    editor.putString(Conf.PREF_WEATHER_ID, weatherId);
                                    editor.apply();
                                    Intent intent = new Intent();
                                    intent.putExtra("city_weather_id", weatherId);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    Snackbar.make(listView, getString(R.string.err_search_area_failed), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.show();
                return true;
            }
        });
        // 开始加载数据
        queryProvinces();
    }

    // 查询省份
    private void queryProvinces() {
        titleText.setText(getString(R.string.settings_select_area));
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(AREA_API_URL, LEVEL_PROVINCE);
        }
    }

    // 查询城市
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = AREA_API_URL + provinceCode;
            queryFromServer(address, LEVEL_CITY);
        }
    }

    // 查询县（最终城市/地区）
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName() + "  [" + county.getWeatherId() + "]");
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = AREA_API_URL + provinceCode + "/" + cityCode;
            queryFromServer(address, LEVEL_COUNTY);
        }
    }

    // 手动搜索地区
    private void querySearchArea(String areaName) {
        if (searchedArea != null) {
            if ("ok".equals(searchedArea.HeWeather5.get(0).status)) {
                titleText.setText(getString(R.string.search_area_result));
                dataList.clear();
                for (SearchedArea.More i : searchedArea.HeWeather5) {
                    dataList.add(i.basic.countryName+"/"+i.basic.provinceName+"/"+i.basic.cityName + " [" + i.basic.weatherId + "]");
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_SEARCH;
            } else {
                Snackbar.make(listView, getString(R.string.err_search_area_failed), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            searchAreaFromServer(Conf.HEWEATHER_SEARCH_AREA_API + "city=" + areaName + "&key=" + Conf.getKey(ChooseAreaActivity.this));
        }

    }

    private void queryFromServer(final String apiAddress, final int type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(apiAddress, new Callback() {
            // 服务器成功响应
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                switch (type) {
                    case LEVEL_PROVINCE:
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case LEVEL_CITY:
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                        break;
                    case LEVEL_COUNTY:
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                        break;
                    default:
                        break;
                }
                if (result) {
                    // 切换到主线程以操作UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case LEVEL_PROVINCE:
                                    queryProvinces();
                                    break;
                                case LEVEL_CITY:
                                    queryCities();
                                    break;
                                case LEVEL_COUNTY:
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 切换到主线程以操作UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Snackbar.make(listView, getString(R.string.err_get_area_list_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void searchAreaFromServer(final String apiAddress) {
        HttpUtil.sendOkHttpRequest(apiAddress, new Callback() {
            // 服务器成功响应
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                searchedArea = new Gson().fromJson(responseText, SearchedArea.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchedArea != null) {
                            querySearchArea(toSearchAreaString);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 切换到主线程以操作UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchedArea = null;
                        closeProgressDialog();
                        Snackbar.make(listView, getString(R.string.err_search_area_failed), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
