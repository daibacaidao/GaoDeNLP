/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.letv.test.testgaodenlp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */


    private final int CLICK_BUTTON1 = 0x01;
    private final int CLICK_BUTTON2 = 0x02;
    private final int CLICK_BUTTON3 = 0x03;

    private final int RETURN_BUTTON1 = 0x01;
    private final int RETURN_BUTTON2 = 0x02;
    private final int RETURN_BUTTON3 = 0x03;

    private final int  PERMISSION_RESULT = 0x01;

    private String keyword = "人民大会堂";

    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;


    private Button mButton1;
    private Button mButton2;
    private Button mButton3;

    private Handler threadHandler;
    private Handler mainHandler;
    private HandlerThread mHandlerThread;

    private LocationManager mLocationManager;
    private Geocoder mGeocoder;

    private double att;
    private double lng;


    AlertDialog alertDialog;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button1:
                    testRequestLocationUpdates();
                    break;
                case R.id.button2:
                    testGetFromLocation();
                    break;
                case R.id.button3:
                    testGetFromLocationName();
                    break;
                default:
                    break;
            }
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MainActivity.this,"定位成功",Toast.LENGTH_SHORT).show();
            Bundle data = new Bundle();
            data.putString("location",location.toString());
            Message msg = Message.obtain(mainHandler,RETURN_BUTTON1);
            msg.setData(data);
            mainHandler.sendMessage(msg);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Toast.makeText(MainActivity.this,"onStatusChanged="+s,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this,"onProviderEnabled="+s,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this,"onProviderDisabled="+s,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        initView();
        initHandler();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGeocoder = new Geocoder(MainActivity.this, Locale.CHINA);
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("thread");
        mHandlerThread.start();
        threadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CLICK_BUTTON1:
                        Log.d("Gaode",mLocationManager.getAllProviders().toString());
                        if ((mLocationManager != null) && (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))) {
                            if(!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                                Toast.makeText(MainActivity.this,"请在设置中开启网络定位",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_RESULT);
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_RESULT);
                                return;
                            }else{
                                Toast.makeText(MainActivity.this,"已获取定位权限，正在定位......",Toast.LENGTH_SHORT).show();
                                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
                            }
                        }else{
                            Toast.makeText(MainActivity.this,"无法网络定位,请在设置中开启网络定位并重启应用",Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case CLICK_BUTTON2:
                        try {
                            List<Address> addrs = mGeocoder.getFromLocation(att,lng,5);
                            if(addrs.size() > 0){
                                Log.d("KKKK","size="+addrs.size());
                                Bundle data = new Bundle();
                                data.putInt("size",addrs.size());
                                for(int i=0;i<addrs.size();i++){
                                    data.putString("addr"+i,"\n"+addrs.get(i).toString());
                                }
                                Message message = Message.obtain(mainHandler,RETURN_BUTTON2);
                                message.setData(data);
                                mainHandler.sendMessage(message);
                            }else{
                                Toast.makeText(MainActivity.this,"找不到查询地点",Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CLICK_BUTTON3:
                        try {
                            List<Address> addrs = mGeocoder.getFromLocationName(keyword, 5);
                            if(addrs.size() > 0){
                                Log.d("KKKK","size="+addrs.size());
                                Bundle data = new Bundle();
                                data.putString("addr",addrs.get(0).toString());
                                Message message = Message.obtain(mainHandler,RETURN_BUTTON3);
                                message.setData(data);
                                mainHandler.sendMessage(message);
                            }else{
                                Toast.makeText(MainActivity.this,"找不到查询地点",Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }
        };

        mainHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case RETURN_BUTTON1:
                        Bundle data1 = msg.getData();
                        String location = data1.getString("location");
                        mTextView1.setText(location);
                        break;
                    case RETURN_BUTTON2:
                        Bundle data2 = msg.getData();
                        int size = data2.getInt("size");
                        String addr1 = "获取"+size+"个地点";
                        for(int i=0;i<size;i++){
                            addr1 +=  data2.getString("addr"+i);
                            addr1 += "\n";
                        }
                        mTextView2.setText(addr1);
                        break;
                    case RETURN_BUTTON3:
                        Bundle data3 = msg.getData();
                        String addr2 = data3.getString("addr");
                        mTextView3.setText(addr2);
                        break;
                    default:
                        break;

                }
                super.handleMessage(msg);
            }
        };

    }

    private void initView() {
        mTextView1 = (TextView)findViewById(R.id.text1);
        mTextView2 = (TextView)findViewById(R.id.text2);
        mTextView3 = (TextView)findViewById(R.id.text3);
        mButton1 = (Button)findViewById(R.id.button1);
        mButton2 = (Button)findViewById(R.id.button2);
        mButton3 = (Button)findViewById(R.id.button3);
        mButton1.setOnClickListener(onClickListener);
        mButton2.setOnClickListener(onClickListener);
        mButton3.setOnClickListener(onClickListener);
    }

    private void testGetFromLocation(){
        alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("请输入经纬度").setView(R.layout.att_lng_dialog)//设置对话框标题
                .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮

                    @Override

                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        // TODO Auto-generated method stub
                        att = Double.valueOf(((EditText)alertDialog.findViewById(R.id.edit_1)).getText().toString());
                        lng = Double.valueOf(((EditText)alertDialog.findViewById(R.id.edit_2)).getText().toString());
                        threadHandler.sendEmptyMessage(CLICK_BUTTON2);
                    }

                }).setNegativeButton("返回",new DialogInterface.OnClickListener() {//添加返回按钮

            @Override

            public void onClick(DialogInterface dialog, int which) {//响应事件
                // TODO Auto-generated method stub
                Log.i("alertdialog"," 请保存数据！");
            }
        }).create();//在按键响应事件中显示此对话框
        alertDialog.show();

    }

    private void testGetFromLocationName(){
        alertDialog =null;
        alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("请输入地理位置名称").setView(R.layout.name_dialog)//设置对话框标题
                .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮

                    @Override

                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        // TODO Auto-generated method stub
                        keyword = ((EditText)alertDialog.findViewById(R.id.edit)).getText().toString();
                        threadHandler.sendEmptyMessage(CLICK_BUTTON3);
                    }

                }).setNegativeButton("返回",new DialogInterface.OnClickListener() {//添加返回按钮

                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件
                        // TODO Auto-generated method stub
                        Log.i("alertdialog"," 请保存数据！");
                    }
                }).create();//在按键响应事件中显示此对话框
        alertDialog.show();

    }

    private void testRequestLocationUpdates(){
        threadHandler.sendEmptyMessage(CLICK_BUTTON1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case  PERMISSION_RESULT:
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
                    Toast.makeText(MainActivity.this,"获取权限成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this,"获取权限失败",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
