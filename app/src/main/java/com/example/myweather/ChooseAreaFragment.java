package com.example.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myweather.db.City;
import com.example.myweather.db.County;
import com.example.myweather.db.Province;
import com.example.myweather.util.Conf;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final String AREA_API_URL = "http://guolin.tech/api/china/";

    // 用于判定当前选择状态的
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    // 各控件
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button buttonBack;
    private Button buttonHelp;
    private ListView listView;

    // 数据
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    // 当前选择状态
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.textView_fca_title);
        buttonBack = (Button) view.findViewById(R.id.button_fca_back);
        buttonHelp = (Button) view.findViewById(R.id.button_fca_hlep);
        listView = (ListView) view.findViewById(R.id.listView_fca_area);
        adapter = new ArrayAdapter<String>(getContext()
                , android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    // 保存设置
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(Conf.PREF_FILE_NAME, getContext().MODE_PRIVATE).edit();
                    editor.putString(Conf.PREF_COUNTY_NAME, countyList.get(i).getCountyName());
                    editor.putString(Conf.PREF_WEATHER_ID, weatherId);
                    editor.apply();
                    //Toast.makeText(getContext(), "set: "+weatherId, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("city_weather_id", weatherId);
                    getActivity().setResult(getActivity().RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });
        // 返回按钮
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        // help button
        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://cdn.heweather.com/china-city-list.txt"));
                startActivity(intent);
            }
        });
        // 开始加载数据
        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("选择省");
        buttonBack.setVisibility(View.GONE);
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
            String address = AREA_API_URL;
            queryFromServer(address, LEVEL_PROVINCE);
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        buttonBack.setVisibility(View.VISIBLE);
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

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        buttonBack.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add("[" + county.getWeatherId() + "]  " + county.getCountyName());
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

    private void queryFromServer(final String address, final int type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
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
                    getActivity().runOnUiThread(new Runnable() {
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "获取省市县列表失败\n" + address, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
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
