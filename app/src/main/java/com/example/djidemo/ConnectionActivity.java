package com.example.djidemo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * @author： DuHongBo
 */
public class ConnectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ConnectionActivity.class.getName();

    private TextView mTextConnectionStatus;
    private TextView mTextProduct;
    private TextView mVersionTv;
    private Button mBtnOpen;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            //添加相机和录音权限
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private EditText urlInputEdit, portInputEdit, urlParaInputEdit, portParaInputEdit, uavNoInputEdit;
    private String showUrl, showPort, showUrlPara, showPortPara, uavNo;
    private Button mAddressBtn;
    private String showUrl_, showPort_, showUrlPara_, showPortPara_, uavNo_;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
        setContentView(R.layout.activity_connection);


        //依靠DatabaseHelper的构造函数创建数据库
        DatabaseHelper dbHelper = new DatabaseHelper(ConnectionActivity.this, "dji_db", null, 1);

        db = dbHelper.getWritableDatabase();

        String sql = "create table IF NOT EXISTS db_uav(urlInput varchar(20),portInput varchar(20),urlParaInput varchar(20),portParaInput varchar(20),uavNoInput varchar(20))";
        db.execSQL(sql);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = db.rawQuery("select urlInput,portInput,urlParaInput,portParaInput,uavNoInput from db_uav", null);
                while (cursor.moveToNext()) {
                    showUrl = cursor.getString(0);
                    showPort = cursor.getString(1);
                    showUrlPara = cursor.getString(2);
                    showPortPara = cursor.getString(3);
                    uavNo = cursor.getString(4);
                }
                cursor.close(); // 关闭游标，释放资源
            }
        });
        thread.start();
        initUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FPVDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    //    检查是否缺少权限，并在需要时请求运行时权限。
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // 缺少权限
        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }

    /**
     * 运行时权限请求的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // 如果有足够的权限，我们将开始注册
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("缺少权限!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("注册中,请等待...");
                    DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                                DJISDKManager.getInstance().startConnectionToProduct();
                                showToast("注册成功!");
                            } else {
                                showToast("注册sdk失败,检查网络是否可用");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("无人机断开连接!");

                        }

                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("无人机已连接");

                        }


                        public void onProductChanged(BaseProduct baseProduct) {

                        }

                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }

                        @Override
                        public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                        }

                        @Override
                        public void onDatabaseDownloadProgress(long l, long l1) {

                        }
                    });
                }
            });
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initUI() {
        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        mTextProduct = (TextView) findViewById(R.id.text_product_info);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnOpen.setOnClickListener(this);
        mBtnOpen.setEnabled(false);
        mVersionTv = (TextView) findViewById(R.id.textView2);
        mVersionTv.setText(getResources().getString(R.string.sdk_version, DJISDKManager.getInstance().getSDKVersion()));
        urlInputEdit = (EditText) findViewById(R.id.edit_url_input);
        urlInputEdit.setText(showUrl);
        urlInputEdit.setOnClickListener(this);

        portInputEdit = (EditText) findViewById(R.id.edit_url_inputPort);
        portInputEdit.setText(showPort);
        portInputEdit.setOnClickListener(this);

        urlParaInputEdit = (EditText) findViewById(R.id.edit_url_input1);
        urlParaInputEdit.setText(showUrlPara);
        urlParaInputEdit.setOnClickListener(this);

        portParaInputEdit = (EditText) findViewById(R.id.edit_url_inputPort1);
        portParaInputEdit.setText(showPortPara);
        portParaInputEdit.setOnClickListener(this);

        uavNoInputEdit = (EditText) findViewById(R.id.edit_uavNo);
        uavNoInputEdit.setText(uavNo);
        uavNoInputEdit.setOnClickListener(this);

        mAddressBtn = (Button) findViewById(R.id.btn_address);
        mAddressBtn.setOnClickListener(this);
        mAddressBtn.setEnabled(false);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = FPVDemoApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {

            mAddressBtn.setEnabled(true);
            String str = mProduct instanceof Aircraft ? "大疆无人机" : "大疆手持设备";
            mTextConnectionStatus.setText("状态: " + str + " 已经连接");

            if (null != mProduct.getModel()) {
                mTextProduct.setText("产品名称:" + mProduct.getModel().getDisplayName());
            } else {
                mTextProduct.setText("产品名称:" + R.string.product_information);
            }

        } else {

            Log.v(TAG, "refreshSDK: False");
            mBtnOpen.setEnabled(false);

            mTextProduct.setText("产品名称:" + R.string.product_information);
            mTextConnectionStatus.setText(R.string.connection_loose);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("showUrl_", showUrl_);
                intent.putExtra("showPort_", showPort_);
                intent.putExtra("showUrlPara_", showUrlPara_);
                intent.putExtra("showPortPara_", showPortPara_);
                intent.putExtra("uavNo_", uavNo_);
                startActivity(intent);
                break;
            }
            case R.id.btn_address: {
                String sqlDelete = "delete from db_uav";
                db.execSQL(sqlDelete);
                showUrl_ = urlInputEdit.getText().toString();
                showPort_ = portInputEdit.getText().toString();
                showUrlPara_ = urlParaInputEdit.getText().toString();
                showPortPara_ = portParaInputEdit.getText().toString();
                uavNo_ = uavNoInputEdit.getText().toString();

                String sqlInsert1 = "insert into db_uav (urlInput,portInput,urlParaInput,portParaInput,uavNoInput) values ('" + showUrl_ + "','" + showPort_ + "','" + showUrlPara_ + "','" + showPortPara_ + "','" + uavNo_ + "')";
                db.execSQL(sqlInsert1);

                showToast("你已经完成输入");

                mBtnOpen.setEnabled(true);


                break;
            }

            default:
                break;
        }
    }

    private void showToast(final String toastMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
