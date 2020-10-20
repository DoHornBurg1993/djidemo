package com.ast.djisdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;


import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;
import java.text.SimpleDateFormat;

import java.util.Date;


import dji.common.battery.BatteryState;

import dji.common.error.DJIError;

import dji.common.flightcontroller.FlightControllerState;

import dji.common.gimbal.GimbalState;
import dji.common.product.Model;

import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;

import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;

import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import dji.sdk.useraccount.UserAccountManager;

/**
 * @author： DuHongBo
 */
public class MainActivity extends Activity implements SurfaceTextureListener, OnClickListener {
    private static final String TAG = MainActivity.class.getName();

    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;

    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private Button returnBtn;
    private TextView recordingTime;
    private TextView distanc;
    private TextView compas;
    private TextView speed;
    private TextView height;
    private TextView battery;
    private TextView longitude;
    private TextView latitude;

    private TextView pitch;
    private TextView roll;
    private TextView yaw;
    //云台的角度
    private float gimbalPitch;
    private float gimbalRoll;
    private float gimbalYaw;

    private TextView remaining_time;
    private TextView temperature;
    private TextView nav_status;
    private TextView mot_status;
    private TextView com_status;
    private TextView time;
    private FlightController mFlightController;
    private Battery mBattery;
    private Compass compass;
    private Gimbal mGimbal;

    private float compass_float = 0.0f, distance = 0, horizontal_distance = 0;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private double home_droneLocationLat = 181, home_droneLocationLng = 181;
    private float altitude = 100.0f, VelocityX = 0, VelocityY = 0, VelocityZ = 0, Velocity = 0, batTemperature;
    private int remainingTime, remainingOil;
    private Camera camera, camera1;
    private Handler handler;
    private String navStatus, motStatus, comStatus;
    private LiveStreamManager liveStreamManager;

    private BaseRequest baseRequest = null;
    private Uav uav = null;


    private Socket mSocket = null;

    private String showUrl_, showPort_, showUrlPara_, showPortPara_, uavNo_;

    private int showPort, showPortPara;

    private String key = null;
    private Thread thread1;
    private JSONObject jsonObject = null;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 1) {
                Toast.makeText(MainActivity.this, "连接失败！", Toast.LENGTH_SHORT).show();

            }
        }
    };

    Handler mHandler1 = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Toast.makeText(MainActivity.this, "成功！", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 1) {
                Toast.makeText(MainActivity.this, "失败！", Toast.LENGTH_SHORT).show();

            }
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        showUrl_ = intent.getStringExtra("showUrl_");
        showPort_ = intent.getStringExtra("showPort_");
        showUrlPara_ = intent.getStringExtra("showUrlPara_");
        showPortPara_ = intent.getStringExtra("showPortPara_");
        uavNo_ = intent.getStringExtra("uavNo_");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        initUI();


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()//磁盘读取操作
                .detectDiskWrites()//磁盘写入操作
                .detectNetwork()//网络操作
                .penaltyLog()//在Logcat中打印违规异常信息
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()//泄露的SqLite对象
                .penaltyLog()
                .penaltyDeath()
                .build());


        // 为摄像头实时查看回调接收原始的H264视频数据
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        mHandler.postDelayed(runnable, 1000);
        //开始任务
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    showPortPara = Integer.parseInt(showPortPara_);
                    Log.e(TAG, "showUrlPara_" + showUrlPara_);
                    Log.e(TAG, "showPortPara" + showPortPara);
//                 mSocket = new Socket("211.149.129.108", 11000);
                    mSocket = new Socket(showUrlPara_, showPortPara);
                    baseRequest = new BaseRequest();
                    baseRequest.setAction("START");
                    baseRequest.setUav_no(uavNo_);
                    Gson gson = new Gson();
                    String JsonBR = gson.toJson(baseRequest) + "SWOOLEFN";
                    OutputStream out = mSocket.getOutputStream();
                    out.write(JsonBR.getBytes());
                    Log.e(TAG, "zzzz");
                    InputStream inputStream = mSocket.getInputStream();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = inputStream.read(buffer);
                    os.write(buffer);
                    jsonObject = new JSONObject(os.toString());
                    Log.e(TAG, "jjj" + jsonObject);
                    mSocket.close();
                } catch (Exception e) {
                }
            }
        });
        thread.start();
        thread1 = new Thread(new Runnable(){
            @Override
            public void run() {
                showPort = Integer.parseInt(showPort_);
                liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
                liveStreamManager.setLiveUrl("rtmp://" + showUrl_ + ":" + showPort + "/uav/" + uavNo_);
                Log.e(TAG, "liveStreamManager" + "rtmp://" + showUrl_ + ":" + showPort + "/uav/" + uavNo_);
//                liveStreamManager.setLiveUrl("rtmp://211.149.129.108:10002/uav/u004");
                liveStreamManager.startStream();
            }
        });
        thread1.start();
    }

    protected void onProductChange() {
        initPreviewer();
        loginAccount();
    }

    private void loginAccount() {
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        showToast("登录成功");
                        Log.e(TAG, "登录成功");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("登录失败:"
                                + error.getDescription());
                        Log.e(TAG, "登录失败");
                    }
                });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "暂停");
        super.onResume();
        initPreviewer();
        onProductChange();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface为空");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "暂停中");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "停止");
        super.onStop();
    }

    public void onReturn(View view) {
        Log.e(TAG, "返回");
        this.finish();
    }

    @Override
    protected void onDestroy() {

        try {
            mSocket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "销毁");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);
        distanc = (TextView) findViewById(R.id.distance);
        compas = (TextView) findViewById(R.id.compass);
        speed = (TextView) findViewById(R.id.speed);
        height = (TextView) findViewById(R.id.height);
        battery = (TextView) findViewById(R.id.battery);
        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        pitch = (TextView) findViewById(R.id.pitch);
        roll = (TextView) findViewById(R.id.roll);
        yaw = (TextView) findViewById(R.id.yaw);
        remaining_time = (TextView) findViewById(R.id.remaining_time);
        temperature = (TextView) findViewById(R.id.temperature);
        recordingTime = (TextView) findViewById(R.id.timer);
        nav_status = (TextView) findViewById(R.id.nav_status);
        mot_status = (TextView) findViewById(R.id.mot_status);
        com_status = (TextView) findViewById(R.id.com_status);
        time = (TextView) findViewById(R.id.time);
        returnBtn = (Button) findViewById(R.id.btn_return);
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }
        returnBtn.setOnClickListener(this);
        recordingTime.setVisibility(View.INVISIBLE);
    }

    private void initPreviewer() {

        BaseProduct product = FPVDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
            mFlightController = ((Aircraft) product).getFlightController();
            mBattery = ((Aircraft) product).getBattery();
            compass = mFlightController.getCompass();
            mGimbal = ((Aircraft) product).getGimbal();

            liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
            //sn
        }
        //云台的角度
        if (mGimbal != null) {
            mGimbal.setStateCallback(new GimbalState.Callback() {
                @Override
                public void onUpdate(GimbalState gimbalState) {
                    gimbalPitch = gimbalState.getAttitudeInDegrees().getPitch();
                    gimbalRoll = gimbalState.getAttitudeInDegrees().getRoll();
                    gimbalYaw = gimbalState.getAttitudeInDegrees().getYaw();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pitch.setText("俯仰角：" + gimbalPitch);
                            roll.setText("翻滚角：" + gimbalRoll);
                            yaw.setText("偏航角：" + gimbalYaw);

                        }
                    });
                }

            });
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    //无人机位置纬度
                    droneLocationLat = flightControllerState.getAircraftLocation().getLatitude();
                    //无人机位置经度
                    droneLocationLng = flightControllerState.getAircraftLocation().getLongitude();
                    //高度
                    altitude = flightControllerState.getAircraftLocation().getAltitude();
                    //返航点纬度
                    home_droneLocationLat = flightControllerState.getHomeLocation().getLatitude();
                    //返航点经度
                    home_droneLocationLng = flightControllerState.getHomeLocation().getLongitude();
                    horizontal_distance = AMapUtils.calculateLineDistance(new LatLng(droneLocationLat, droneLocationLng), new LatLng(home_droneLocationLat, home_droneLocationLng));
                    //距离
                    distance = (float) Math.sqrt(altitude * altitude + horizontal_distance * horizontal_distance);
                    if (null != compass) {
                        compass_float = compass.getHeading();
                    }
                    //X轴上的速度
                    VelocityX = flightControllerState.getVelocityX();
                    //Y轴上的速度
                    VelocityY = flightControllerState.getVelocityY();
                    //Z轴上的速度
                    VelocityZ = flightControllerState.getVelocityZ();
                    //速度
                    Velocity = (float) Math.sqrt(VelocityX * VelocityX + VelocityY * VelocityY + VelocityZ * VelocityZ);
                    //最大留空时间
                    remainingTime = flightControllerState.getGoHomeAssessment().getRemainingFlightTime();
                    //动力系统是否正常
                    if (flightControllerState.areMotorsOn()) {
                        motStatus = "正常";
                    } else {
                        motStatus = "故障";
                    }
                    //导航系统是否正常
                    if (flightControllerState.isMultipleModeOpen()) {
                        navStatus = "故障";
                    } else {
                        navStatus = "正常";
                    }
                    //通信系统状态是否正常(这里是遥控器和飞机之间的连接),信号丢失则为true
                    if (flightControllerState.isFailsafeEnabled()) {
                        comStatus = "故障";
                    } else {
                        comStatus = "正常";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distanc.setText("距离：" + distance);
//                            Log.d(TAG, "距离：" + distance);
                            compas.setText("朝向：" + compass_float);
//                            Log.d(TAG, "朝向：" + compass_float);
                            speed.setText("速度：" + Velocity);
//                            Log.d(TAG, "速度：" + Velocity);
                            height.setText("高度：" + altitude);
//                            Log.d(TAG, "高度：" + altitude);
                            longitude.setText("飞机经度:" + droneLocationLng);
//                            Log.d(TAG, "飞机经度:" + droneLocationLng);
                            latitude.setText("飞机纬度:" + droneLocationLat);
//                            Log.d(TAG, "飞机纬度:" + droneLocationLat);
                            remaining_time.setText("最大留空时间:" + remainingTime);
//                            Log.d(TAG, "最大留空时间:" + remainingTime);
                            nav_status.setText("导航状态:" + navStatus);
//                            Log.d(TAG, "导航状态:" + navStatus);
                            mot_status.setText("动力状态:" + motStatus);
//                            Log.d(TAG, "动力状态:" + motStatus);
                            com_status.setText("通信状态:" + comStatus);
                        }
                    });
                }
            });
        }
        if (mBattery != null) {
            mBattery.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
                    remainingOil = batteryState.getChargeRemainingInPercent();
                    batTemperature = batteryState.getTemperature();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            battery.setText("电量：" + remainingOil);
//                            Log.d(TAG, "电量：" + remainingOil);
                            temperature.setText("温度:" + batTemperature);
//                            Log.d(TAG, "温度:" + batTemperature);
                        }
                    });
                }
            });
        }
    }

    private void uninitPreviewer() {
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                showPortPara = Integer.parseInt(showPortPara_);
//                Log.e(TAG, "showUrlPara" + showUrlPara);
//                Log.e(TAG, "showPortPara" + showPortPara);
//                mSocket = new Socket("211.149.129.108", 11000);
                mSocket = new Socket(showUrlPara_, showPortPara);
                key = jsonObject.getString("key");
                Log.e(TAG, "keykey " + key);
                OutputStream out = mSocket.getOutputStream();
                uav = new Uav();
                //飞行命令
                uav.setAction("UAVDATA");
                //key
                uav.setKey(key);
                //飞机编号
                uav.setUav_no(uavNo_);
                //飞行状态
                uav.setFly_status(1);
                //时间
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                uav.setTime(df.format(new Date()));
                //经度
                uav.setLon(droneLocationLng);
                //纬度
                uav.setLat(droneLocationLat);
                //海拔高度
//                uav.setAlt();
                //地面高度
                uav.setGround_alt(altitude);
                //航向角
                uav.setCourse(compass_float);
                //俯仰角
                uav.setPitch(Double.parseDouble(String.valueOf(gimbalPitch)));
//                uav.setPitch(altPitch);
                //翻滚角
                uav.setRoll(Double.parseDouble(String.valueOf(gimbalRoll)));
//                uav.setRoll(altRoll);
                //偏航角
                uav.setYaw(Double.parseDouble(String.valueOf(gimbalYaw)));
//                uav.setYaw(altYaw);
                //真空速
//                uav.setTrue_airspeed();
                //地速
                uav.setGround_speed(Velocity);
                //剩余电量
                uav.setRemaining_oil(remainingOil);
                //剩余航程
//                uav.setRemaining_dis();
                //剩余时间
                uav.setRemaining_time(Double.valueOf(remainingTime / 60));
                //动力系统状态
                if ("正常" == motStatus) {
                    uav.setMot_status(0);
                } else {
                    uav.setMot_status(1);
                }
                //导航系统状态
                if ("正常" == navStatus) {
                    uav.setNav_status(0);
                } else {
                    uav.setNav_status(1);
                }
                //通信系统状态
                if ("正常" == comStatus) {
                    uav.setCom_status(0);
                } else {
                    uav.setCom_status(1);
                }
                //温度
                uav.setTemperature(Double.valueOf(batTemperature));
                //湿度
//                uav.setHumidity();
                //风速
//                uav.setWind_speed();
                //距离
//                uav.setDistance(distance);
                Gson gson = new Gson();
                String JsonUav = gson.toJson(uav) + "SWOOLEFN";
                out.write(JsonUav.getBytes());
                uav = null;
                mSocket.close();
            } catch (Exception e) {
            }
            mHandler1.postDelayed(this, 5000);
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_return: {
                AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                normalDialog.setMessage("确认退出返回主页吗?该操作将导致飞行任务终止!");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    showPortPara = Integer.parseInt(showPortPara_);
//                Log.e(TAG, "showUrlPara" + showUrlPara);
//                Log.e(TAG, "showPortPara" + showPortPara);
//                mSocket = new Socket("211.149.129.108", 11000);
                                    mSocket = new Socket(showUrlPara_, showPortPara);
                                    baseRequest = new BaseRequest();
                                    baseRequest.setAction("END");
                                    baseRequest.setUav_no(uavNo_);
                                    Gson gson = new Gson();
                                    String JsonBR = gson.toJson(baseRequest) + "SWOOLEFN";
                                    OutputStream out = mSocket.getOutputStream();

                                    out.write(JsonBR.getBytes());
                                    InputStream inputStream = mSocket.getInputStream();
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int len = inputStream.read(buffer);
                                    os.write(buffer);
                                    jsonObject = new JSONObject(os.toString());
                                    Integer status = jsonObject.getInt("status");

                                    if (status == 0) {
                                        os.close();
                                    }
                                    mSocket.close();
                                } catch (Exception e) {
                                }
                                finish();
                                System.exit(0);
                            }

                        });
                normalDialog.setNegativeButton("关闭",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                normalDialog.show();
            }
            default:
                break;
        }
    }


}
