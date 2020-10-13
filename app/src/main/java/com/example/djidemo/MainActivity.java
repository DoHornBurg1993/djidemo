package com.example.djidemo;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;
import com.qx.wz.dj.rtcm.StringUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import dji.common.battery.BatteryState;

import dji.common.camera.SystemState;
import dji.common.error.DJIError;

import dji.common.flightcontroller.FlightControllerState;

import dji.common.flightcontroller.flyzone.UnlockedZoneGroup;
import dji.common.gimbal.GimbalState;
import dji.common.product.Model;

import dji.common.remotecontroller.Information;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.internal.flighthub.FlightHubAuthInterceptor;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flighthub.FlightHubManager;
import dji.sdk.flighthub.model.FlightHistoricalDetail;
import dji.sdk.flighthub.model.RealTimeFlightData;
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

    //sn
    private BaseComponent baseComponent;


    //sn

    protected TextureView mVideoSurface = null;
    private Button returnBtn, liveBtn;
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
    private dji.common.remotecontroller.Information information;
    private float compass_float = 0.0f, distance = 0, horizontal_distance = 0;
    private double droneLocationLat = 181, droneLocationLng = 181, altPitch, altRoll, altYaw;
    private double home_droneLocationLat = 181, home_droneLocationLng = 181;
    private float altitude = 100.0f, VelocityX = 0, VelocityY = 0, VelocityZ = 0, Velocity = 0, batTemperature;
    private int remainingTime, remainingOil;
    private Camera camera, camera1;
    private Handler handler;
    private String navStatus, motStatus, comStatus;
    private LiveStreamManager liveStreamManager;
    private String originAddress;
    private BaseRequest baseRequest=null;
    private Uav uav=null;

    //socket
    private Socket mSocket = null;
    private BufferedReader mBufferedReaderClient = null;
    private PrintWriter mPrintWriterClient = null;
    private String Information;
    private String strInputstream;
    private String key=null;
    private Thread thread1;
    private JSONObject jsonObject=null;
    //socket


    //Socket
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
    //Socket


//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            String result = "";
//
//            if ("OK".equals(msg.obj.toString())) {
//                result = "发送成功!";
//            } else if ("Wrong".equals(msg.obj.toString())) {
//                result = "发送失败!";
//            } else {
//                result = msg.obj.toString();
//            }
////            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
//        }
//    };

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

        //Socket
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
        //Socket

        // 为摄像头实时查看回调接收原始的H264视频数据
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

//        Camera camera = FPVDemoApplication.getCameraInstance();
//
//        if (camera != null) {
//
//            camera.setSystemStateCallback(new SystemState.Callback() {
//                @Override
//                public void onUpdate(SystemState cameraSystemState) {
//                    if (null != cameraSystemState) {
//
//                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
//                        int minutes = (recordTime % 3600) / 60;
//                        int seconds = recordTime % 60;
//
//                        final String timeString = String.format("%02d:%02d", minutes, seconds);
//                        final boolean isVideoRecording = cameraSystemState.isRecording();
//
//                        MainActivity.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                recordingTime.setText(timeString);
//
//                                /*
//                                 * 更新recordingTime TextView的可见性和mRecordBtn的检查状态
//                                 */
//                                if (isVideoRecording) {
//                                    recordingTime.setVisibility(View.VISIBLE);
//                                } else {
//                                    recordingTime.setVisibility(View.INVISIBLE);
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//
//        }
        mHandler.postDelayed(runnable, 1000);

        //socket
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    mSocket = new Socket("192.168.31.7", 11000);
                    mSocket = new Socket("211.149.129.108", 11000);
                    baseRequest= new BaseRequest();
                    baseRequest.setAction("START");
                    baseRequest.setUav_no("u004");
                    Gson gson = new Gson();
                    String JsonBR = gson.toJson(baseRequest)+"SWOOLEFN";
//                    String str = "{\n" +
//                            "  \"action\": \"START\",\n" +
//                            "  \"uav_no\": \"u001\"\n" +
//                            "  \"time\": \"2020-09-23 22:02:00\",\n" +
//                            "\"airline_no\": \" AR_20200713142514\",\n" +
//                            "  \"lon\": 150.7840271,\n" +
//                            "  \"lat\": 108.4068375,\n" +
//                            "    \"alt\": 1154383.872,\n" +
//                            "  \"waypoints\": [{\n" +
//                            "	\"id\": \" 1\",\n" +
//                            "   \"lon\": 150.7840271,\n" +
//                            "   \"lat\": 108.4068375,\n" +
//                            "     \"alt\": 115.4383872,\n" +
//                            "\"ground_alt\":100\n" +
//                            "},{\n" +
//                            "\"id\": \" 2\", \n" +
//                            "   \"lon\": 150.7840271,\n" +
//                            "   \"lat\": 108.4068375,\n" +
//                            "     \"alt\": 115.4383872,\n" +
//                            "\"ground_alt\":100\n" +
//                            "}]\n" +
//                            "}SWOOLEFN ";

                    OutputStream out = mSocket.getOutputStream();

                    out.write(JsonBR.getBytes());

//                    thread1.start();
                    Log.e(TAG, "zzzz");
                    InputStream inputStream = mSocket.getInputStream();

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

//                    os.write(buffer);


                    int len = inputStream.read(buffer);
                    os.write(buffer);

                    jsonObject = new JSONObject(os.toString());

                    Log.e(TAG, "jjj" + jsonObject);

                    mSocket.close();

//                    StringBuffer outx = new StringBuffer();
//                    byte[] b = new byte[4096];
//                    for (int n; (n = inputStream.read(b)) != -1;) {
//                        outx.append(new String(b, 0, n));
//                    }
//                    out.toString();
//                    Log.e(TAG, "xxxx"+ out.toString());


//                    BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
//                    Log.e(TAG, "qqq:"+br.readLine());


                } catch (Exception e) {
                }
//                }

            }
        });
        thread.start();
//        thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(5000);
//
//                    mSocket = new Socket("192.168.31.7", 11000);
//                    InputStream inputStream=mSocket.getInputStream();
//
//                    ByteArrayOutputStream os=new ByteArrayOutputStream();
//
//
//
//                    byte[] buffer=new byte[1024];
//
////                    os.write(buffer);
//                    Log.e(TAG, "rrr11"+inputStream.available() );
//
//                    int len = inputStream.read(buffer);
//
//
//                    while(len!=-1){
//
//                        os.write(buffer);
//                    }
//
//                    JSONObject jsonObject=new JSONObject(os.toString());
//
//                    Log.e(TAG, "jjj"+jsonObject);
//
//                    mSocket.close();
//                }catch (Exception e){
//
//                }

//            }
//        });
//        thread1.start();


        //socket

    }

    //socket 发送消息
//    private void Send() {
//        try {
//            // 发送数据给服务端
//            OutputStream outputStream = socket.getOutputStream();
//            outputStream.write("abc".getBytes("gb2312"));
//            // socket.shutdownOutput();
//            // 等待服务器发送数据，读取数据（超时异常）
//            DataInputStream br = new DataInputStream(socket.getInputStream());
//            byte[] b = new byte[1024];
//            int length = br.read(b);
//            String Msg = new String(b, 0, length, "gb2312");
//            System.out.println(Msg + "    接收到服务器的数据");
//        } catch (UnknownHostException e) {
//            System.out.println("UnknownHost  来自服务器的数据");
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.out.println("IOException   来自服务器的数据");
//            e.printStackTrace();
//        }
//    }

    //socket

    //Socket
//    private String getInfoBuff(char[] buff, int count) {
//        char[] temp = new char[count];
//        for (int i = 0; i < count; i++) {
//            temp[i] = buff[i];
//        }
//        return new String(temp);
//    }
    //Socket


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

        //socket
        try {
            mSocket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }



        //socket


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
        liveBtn = (Button) findViewById(R.id.btn_live);


        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        returnBtn.setOnClickListener(this);
        liveBtn.setOnClickListener(this);
        recordingTime.setVisibility(View.INVISIBLE);

    }
//    private void getDjiId(){
//        BaseProduct mProduct = FPVDemoApplication.getProductInstance();
//    }
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

        //
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

        //sn
//        if(mRemoteController!=null){
//            mRemoteController.getIndex();
//        }
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



            //Socket
            try {
//                mSocket = new Socket("192.168.31.7", 11000);
                mSocket = new Socket("211.149.129.108", 11000);
                key=jsonObject.getString("key");
                Log.e(TAG, "keykey "+key);
                OutputStream out = mSocket.getOutputStream();


                uav=new Uav();
                //飞行命令
                uav.setAction("UAVDATA");
                //key
                uav.setKey(key);
                //飞机编号
                uav.setUav_no("u004");
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
                uav.setPitch(altPitch);
                //翻滚角
                uav.setRoll(altRoll);
                //偏航角
                uav.setYaw(altYaw);
                //真空速
//                uav.setTrue_airspeed();
                //地速
                uav.setGround_speed(Velocity);
                //剩余电量
                uav.setRemaining_oil(remainingOil);
                //剩余航程
//                uav.setRemaining_dis();
                //剩余时间
                uav.setRemaining_time(Double.valueOf(remainingTime/60));
                //动力系统状态
                if("正常"==motStatus){
                    uav.setMot_status(0);
                }else{
                    uav.setMot_status(1);
                }
                //导航系统状态
                if("正常"==navStatus){
                    uav.setNav_status(0);
                }else{
                    uav.setNav_status(1);
                }
                //通信系统状态
                if("正常"==comStatus){
                    uav.setCom_status(0);
                }else{
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
                String JsonUav = gson.toJson(uav)+"SWOOLEFN";
                //                                String str = "{\n" +
//                        "\"action\":\"UAVDATA\",\n" +
//                        "\"key\":\""+key+"\",\n" +
//                        "\"uav_no\":\"u001\",\n" +
//                        "\"fly_status\":1,\n" +
//                        "\"time\":\"2019-7-1 17:00:00\",\n" +
//                        "\"lon\":150.7840271,\n" +
//                        "\"lat\":108.4068375,\n" +
//                        "\"alt\":1154383.872,\n" +
//                        "\"ground_alt\":0.0,\n" +
//                        "\"course\":0.0,\n" +
//                        "\"pitch\":1.5,\n" +
//                        "\"roll\":1.5,\n" +
//                        "\"true_airspeed\":0.0,\n" +
//                        "\"ground_speed\":0.0,\n" +
//                        "\"remaining_oil\":80.0,\n" +
//                        "\"remaining_dis\":500.0,\n" +
//                        "\"remaining_time\":80.0,\n" +
//                        "\"mot_status\":0,\n" +
//                        "\"nav_status\":0,\n" +
//                        "\"temperature\":12.3,\n" +
//                        "\"humidity\":12.3,\n" +
//                        "\"wind_speed\":0.0\n" +
//                        "}SWOOLEFN";
                Log.e(TAG, "kkkkkkkk");
                out.write(JsonUav.getBytes());
                uav=null;//gc

                mSocket.close();

            } catch (Exception e) {
            }
//            //Socket
//
            mHandler1.postDelayed(this, 10000);

        }

    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_return: {
                   finish();
                System.exit(0);
//                mHandler1.removeCallbacks(runnable);
//                mHandler.removeCallbacks(runnable);
//                try {
//                    mSocket.shutdownOutput();
//                }catch (Exception e){
//
//                }
//                Intent intent = new Intent(this, ConnectionActivity.class);
//                startActivity(intent);
//                break;
            }
            case R.id.btn_live: {
                if (liveStreamManager != null) {
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
                    } else {
                        final EditText editText = new EditText(MainActivity.this);
                        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
                        inputDialog.setTitle("请输入直播地址：").setView(editText);
                        inputDialog.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        liveStreamManager.setAudioStreamingEnabled(false);
//                                        editText.setText("rtmp://192.168.31.6:1935/live");
//                                        liveStreamManager.setLiveUrl(editText.getText().toString());
                                        liveStreamManager.setLiveUrl("rtmp://211.149.129.108:10002/uav/u004");
//                                        liveStreamManager.setLiveUrl("rtmp://192.168.31.6:1935/live");
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
                } else
                    showToast("直播不可用！");
                break;
            }

            default:
                break;
        }
    }


}
