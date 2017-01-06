package com.mwh.hotweather;

import android.app.ProgressDialog;
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

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mwh.hotweather.db.City;
import com.mwh.hotweather.db.County;
import com.mwh.hotweather.db.Province;
import com.mwh.hotweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Youg on 2017/1/6 11:12
 * 选择地址
 */

public class ChooseAreaFragment extends Fragment {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.back_button)
    Button backButton;
    @BindView(R.id.list_view)
    ListView listView;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countList;//县列表

    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市

    private int currentLevel;//当前选中的级别

    String address_url = "http://guolin.tech/api/china/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        ButterKnife.bind(this, view);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        //查询省份信息
        queryProvinces();
    }

    /**
     * 查询省份，先查询数据库，如果没有就查询服务器
     */
    private void queryProvinces() {
        title.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province :
                    provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFormSerer(address_url, "province");
        }
    }

    /**
     * 查询省中的市
     */
    private void queryCities() {
        title.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city :
                    cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = address_url + provinceCode;
            queryFormSerer(address, "city");
        }
    }

    /**
     * 查询选中市中的所有县
     */
    private void queryCounties() {
        title.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countList.size() > 0) {
            dataList.clear();
            for (County county :
                    countList) {
                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = address_url + provinceCode + "/" + cityCode;
            queryFormSerer(address, "county");
        }
    }

    /**
     * 从服务器获得城市数据并查询
     *
     * @param address
     * @param type
     */
    private void queryFormSerer(String address, final String type) {
        showProgressDialog();
        OkGo.get(address)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        boolean result = false;
                        if ("province".equals(type)) {
                            result = Utility.handleProvinceResponse(s);
                        } else if ("city".equals(type)) {
                            result = Utility.handleCityResponse(s, selectedProvince.getId());
                        } else if ("county".equals(type)) {
                            result = Utility.handleCountyResponse(s, selectedCity.getId());
                        }
                        if (result) {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
