package com.example.djidemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.djidemo.http.HttpCallbackListener;
import com.example.djidemo.http.HttpUtil;
import com.example.djidemo.http.Uav;

import java.util.HashMap;

import dji.common.battery.BatteryState;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.rtk.NetworkServiceSettings;

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
import dji.sdk.remotecontroller.RemoteController;
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
    private Button returnBtn,liveBtn;
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
    private RemoteController mRemoteController;
    private float compass_float = 0.0f, distance = 0, horizontal_distance = 0;
    private double droneLocationLat = 181, droneLocationLng = 181, altPitch, altRoll, altYaw;
    private double home_droneLocationLat = 181, home_droneLocationLng = 181;
    private float altitude = 100.0f, VelocityX = 0, VelocityY = 0, VelocityZ = 0, Velocity = 0;
    private int remainingTime;
    private Camera camera, camera1;
    private Handler handler;
    private String navStatus, motStatus, comStatus;
    private LiveStreamManager liveStreamManager;
    private String originAddress;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String result = "";

            if ("OK".equals(msg.obj.toString())) {
                result = "发送成功!";
            } else if ("Wrong".equals(msg.obj.toString())) {
                result = "发送失败!";
            } else {
                result = msg.obj.toString();
            }
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        originAddress = "http://" + address + ":8333/point/getTest";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        initUI();

        // 为摄像头实时查看回调接收原始的H264视频数据
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = FPVDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * 更新recordingTime TextView的可见性和mRecordBtn的检查状态
                                 */
                                if (isVideoRecording) {
                                    recordingTime.setVisibility(View.VISIBLE);
                                } else {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }
        mHandler.postDelayed(runnable, 1000);

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
        returnBtn=(Button)findViewById(R.id.btn_return);
        liveBtn=(Button)findViewById(R.id.btn_live);


        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        returnBtn.setOnClickListener(this);
        liveBtn.setOnClickListener(this);
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
            mRemoteController = ((Aircraft) product).getRemoteController();
            liveStreamManager= DJISDKManager.getInstance().getLiveStreamManager();
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
                    ;
                    //导航系统是否正常
                    if (flightControllerState.isMultipleModeOpen()) {
                        navStatus = "故障";
                    } else {
                        navStatus = "正常";
                    }
                    ;
                    //通信系统状态是否正常(这里是遥控器和飞机之间的连接),信号丢失则为true
                    if (flightControllerState.isFailsafeEnabled()) {
                        comStatus = "故障";
                    } else {
                        comStatus = "正常";
                    }
                    ;
                    //俯仰角
                    altPitch = flightControllerState.getAttitude().pitch;
                    //翻滚角
                    altRoll = flightControllerState.getAttitude().roll;
                    //偏航角
                    altYaw = flightControllerState.getAttitude().yaw;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distanc.setText("距离：" + distance);
                            Log.d(TAG, "距离：" + distance);
                            compas.setText("朝向：" + compass_float);
                            Log.d(TAG, "朝向：" + compass_float);
                            speed.setText("速度：" + Velocity);
                            Log.d(TAG, "速度：" + Velocity);
                            height.setText("高度：" + altitude);
                            Log.d(TAG, "高度：" + altitude);
                            longitude.setText("飞机经度:" + droneLocationLng);
                            Log.d(TAG, "飞机经度:" + droneLocationLng);
                            latitude.setText("飞机纬度:" + droneLocationLat);
                            Log.d(TAG, "飞机纬度:" + droneLocationLat);
                            remaining_time.setText("最大留空时间:" + remainingTime);
                            Log.d(TAG, "最大留空时间:" + remainingTime);
                            nav_status.setText("导航状态:" + navStatus);
                            Log.d(TAG, "导航状态:" + navStatus);
                            mot_status.setText("动力状态:" + motStatus);
                            Log.d(TAG, "动力状态:" + motStatus);
                            com_status.setText("通信状态:" + comStatus);
                            Log.d(TAG, "通信状态:" + comStatus);
                            pitch.setText("飞机俯仰角：" + altPitch);
                            Log.d(TAG, "飞机俯仰角：" + altPitch);
                            roll.setText("飞机翻滚角：" + altRoll);
                            Log.d(TAG, "飞机翻滚角：" + altRoll);
                            yaw.setText("飞机偏航角：" + altYaw);
                            Log.d(TAG, "飞机偏航角：" + altYaw);
                        }
                    });
                }
            });

        }

        if (mBattery != null) {
            mBattery.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            battery.setText("电量：" + batteryState.getChargeRemainingInPercent());
                            Log.d(TAG, "电量：" + batteryState.getChargeRemainingInPercent());
                            temperature.setText("温度:" + batteryState.getTemperature());
                            Log.d(TAG, "温度:" + batteryState.getTemperature());
                        }
                    });

                }
            });
        }
    }

    private void uninitPreviewer() {
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            // 重置回调
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
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

            //构造HashMap
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(Uav.LON, droneLocationLng + "");
            params.put(Uav.LAT, droneLocationLat + "");
            try {
                //构造完整URL
                String compeletedURL = HttpUtil.getURLWithParams(originAddress, params);
                Log.d(TAG, "run: " + compeletedURL);
                //发送请求
                HttpUtil.sendHttpRequest(compeletedURL, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        Message message = new Message();
                        message.obj = response;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(Exception e) {
                        Message message = new Message();
                        message.obj = e.toString();
                        mHandler.sendMessage(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(this, 1000);

        }


    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_return:{
                mHandler.removeCallbacks(runnable);
                Intent intent = new Intent(this, ConnectionActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_live:{
                if(liveStreamManager!=null) {
                    if (liveStreamManager.isStreaming()) {
                        AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                        normalDialog.setMessage("停止直播?");
                        normalDialog.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        liveStreamManager.stopStream();
                                        liveBtn.setText("开始直播！");
                                    }
                                });
                        normalDialog.setNegativeButton("关闭",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        // 显示
                        normalDialog.show();
                    }else{
                        final EditText editText = new EditText(MainActivity.this);
                        AlertDialog.Builder inputDialog =new AlertDialog.Builder(MainActivity.this);
                        inputDialog.setTitle("请输入直播地址：").setView(editText);
                        inputDialog.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        liveStreamManager.setAudioStreamingEnabled(false);
                                        liveStreamManager.setLiveUrl(editText.getText().toString());
                                        liveStreamManager.startStream();
                                        liveBtn.setText("直播中。。。");
                                    }
                                });
                        inputDialog.setNegativeButton("关闭",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    }
                }else
                    showToast("直播不可用！");

                break;
            }

            default:
                break;
        }
    }


}
