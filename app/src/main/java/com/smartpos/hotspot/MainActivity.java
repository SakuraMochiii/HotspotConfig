package com.smartpos.hotspot;


import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText networkName, et_password;
    private RadioButton none, wpa2;
    private WifiManager mWifiManager;// = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    private TextView tv_password;
    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        none = (RadioButton) findViewById(R.id.rb_security_none);
        wpa2 = (RadioButton) findViewById(R.id.rb_security_wpa2);

        aSwitch = findViewById(R.id.switch1);
        networkName = findViewById(R.id.hotspotname);
        et_password = findViewById(R.id.et_password);
        tv_password = findViewById(R.id.tv_password);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        aSwitch.setOnClickListener(this);
        none.setOnClickListener(this);
        wpa2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch1:
                if (aSwitch.isChecked()) {
                    if (et_password.getText().toString().trim().length() < 8) {
                        Toast.makeText(this, "The password must have at least 8 characters.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    setWlanhotspot(networkName, et_password);
                    none.setEnabled(false);
                    wpa2.setEnabled(false);
                    networkName.setEnabled(false);
                    et_password.setEnabled(false);
                    Log.d("TAG", "enable hotspot");
                    Toast.makeText(this, "enabling hotspot.", Toast.LENGTH_LONG).show();
                } else {
                    none.setEnabled(true);
                    wpa2.setEnabled(true);
                    networkName.setEnabled(true);
                    et_password.setEnabled(true);
                    starthotspot(null, null, false);
                    Log.d("TAG", "disable hotspot");
                    Toast.makeText(this, "diable hotspot.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rb_security_none:
                tv_password.setVisibility(View.GONE);
                et_password.setVisibility(View.GONE);
                break;
            case R.id.rb_security_wpa2:
                tv_password.setVisibility(View.VISIBLE);
                et_password.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setWlanhotspot(EditText networkName, EditText password) {
        String name = "";
        String pwd = "";
        if (networkName != null && networkName.length() > 1) {
            name = networkName.getText().toString();
        }
        if (password != null && password.length() > 1) {
            pwd = password.getText().toString();
        }
        if (none.isChecked()) {
            starthotspot(name, null, true);
        } else if (wpa2.isChecked()) {

            starthotspot(name, pwd, true);
        }
    }

    public void starthotspot(String name, String pwd, boolean enable) {
        Log.d("TAG", "starthotspot, name=" + name + ",pwd=" + pwd);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Method methodaa = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                    methodaa.invoke(mWifiManager, null, false);
                    if (!enable) {
                        return;
                    }
                    WifiConfiguration apConfig = new WifiConfiguration();
                    apConfig.SSID = name;
                    apConfig.preSharedKey = pwd;
                    if (pwd != null) {
                        if (Build.VERSION.SDK_INT > 23) {
                            apConfig.allowedKeyManagement.set(4);//WifiConfiguration.KeyMgmt.WPA2_PSK   Q2 6.0 = 6,  Q2A7+Q3A7 = 4;
                        } else {
                            apConfig.allowedKeyManagement.set(6);//WifiConfiguration.KeyMgmt.WPA2_PSK   Q2 6.0 = 6,  Q2A7+Q3A7 = 4;
                        }
                    } else {
                        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    }
//                    apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                    method.invoke(mWifiManager, apConfig, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}