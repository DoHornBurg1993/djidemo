package com.ast.djisdk;

/**
 * @author： DuHongBo
 */
public class Uav {
    /** START：开始任务 END：结束任务 UAVDATA：实时飞行数据 */
    private String action;
    /**key*/
    private String key;
    /** 飞机编号 */
    private String uav_no;
    /** 0:就绪(起飞前检查完成),1:起飞中(从解锁到起飞航线执行完),2:任务中(在任务航线列表中),3.	飞行中，4:降落中(进降落航线加锁前),5:已着陆(已加锁) */
    private int fly_status;

    /** 当前时间 */
    private String time;

    /** 飞机经度 */
    private Double lon;

    /** 飞机纬度 */
    private Double lat;

    /** 飞机海拔高度,单位米 */
    private Double alt;

    /** 飞行场高,单位米 */
    private float ground_alt;

    /** 飞机航向角 */
    private float course;

    /** 飞机俯仰角 */
    private Double pitch;

    /** 飞机横滚角 */
    private Double roll;

    /** 飞机偏航角 */
    private Double yaw;

    /** 飞机真空速,km/h */
    private Double true_airspeed;

    /** 飞机地速,km/h */
    private float ground_speed;

    /** 剩余电量,百分比,80.0为80% */
    private int remaining_oil;

    /** 最大剩余航程,单位千米 */
    private Double remaining_dis;

    /** 最大留空时间,分钟 */
    private Double remaining_time;

    /** 动力系统状态0正常,1故障 */
    private Integer mot_status;

    /** 导航系统状态0正常,1故障 */
    private Integer nav_status;

    /** 通信系统状态0正常,1故障 */
    private Integer com_status;

    /** 温度,摄氏度 */
    private Double temperature;

    /** 湿度，百分比 */
    private Double humidity;

    /** 风速,m/s */
    private Double wind_speed;

//    /** 距离,m */
//    private float distance;


    public Uav(String action, String key, String uav_no, int fly_status, String time, Double lon, Double lat, Double alt, float ground_alt, float course, Double pitch, Double roll, Double yaw, Double true_airspeed, float ground_speed, int remaining_oil, Double remaining_dis, Double remaining_time, Integer mot_status, Integer nav_status, Integer com_status, Double temperature, Double humidity, Double wind_speed) {
        this.action = action;
        this.key = key;
        this.uav_no = uav_no;
        this.fly_status = fly_status;
        this.time = time;
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
        this.ground_alt = ground_alt;
        this.course = course;
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
        this.true_airspeed = true_airspeed;
        this.ground_speed = ground_speed;
        this.remaining_oil = remaining_oil;
        this.remaining_dis = remaining_dis;
        this.remaining_time = remaining_time;
        this.mot_status = mot_status;
        this.nav_status = nav_status;
        this.com_status = com_status;
        this.temperature = temperature;
        this.humidity = humidity;
        this.wind_speed = wind_speed;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUav_no() {
        return uav_no;
    }

    public void setUav_no(String uav_no) {
        this.uav_no = uav_no;
    }

    public int getFly_status() {
        return fly_status;
    }

    public void setFly_status(int fly_status) {
        this.fly_status = fly_status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public float getGround_alt() {
        return ground_alt;
    }

    public void setGround_alt(float ground_alt) {
        this.ground_alt = ground_alt;
    }

    public float getCourse() {
        return course;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public Double getPitch() {
        return pitch;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    public Double getRoll() {
        return roll;
    }

    public void setRoll(Double roll) {
        this.roll = roll;
    }

    public Double getYaw() {
        return yaw;
    }

    public void setYaw(Double yaw) {
        this.yaw = yaw;
    }

    public Double getTrue_airspeed() {
        return true_airspeed;
    }

    public void setTrue_airspeed(Double true_airspeed) {
        this.true_airspeed = true_airspeed;
    }

    public float getGround_speed() {
        return ground_speed;
    }

    public void setGround_speed(float ground_speed) {
        this.ground_speed = ground_speed;
    }

    public int getRemaining_oil() {
        return remaining_oil;
    }

    public void setRemaining_oil(int remaining_oil) {
        this.remaining_oil = remaining_oil;
    }

    public Double getRemaining_dis() {
        return remaining_dis;
    }

    public void setRemaining_dis(Double remaining_dis) {
        this.remaining_dis = remaining_dis;
    }

    public Double getRemaining_time() {
        return remaining_time;
    }

    public void setRemaining_time(Double remaining_time) {
        this.remaining_time = remaining_time;
    }

    public Integer getMot_status() {
        return mot_status;
    }

    public void setMot_status(Integer mot_status) {
        this.mot_status = mot_status;
    }

    public Integer getNav_status() {
        return nav_status;
    }

    public void setNav_status(Integer nav_status) {
        this.nav_status = nav_status;
    }

    public Integer getCom_status() {
        return com_status;
    }

    public void setCom_status(Integer com_status) {
        this.com_status = com_status;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getWind_speed() {
        return wind_speed;
    }

    public void setWind_speed(Double wind_speed) {
        this.wind_speed = wind_speed;
    }

    @Override
    public String toString() {
        return "Uav{" +
                "action='" + action + '\'' +
                ", key='" + key + '\'' +
                ", uav_no='" + uav_no + '\'' +
                ", fly_status=" + fly_status +
                ", time='" + time + '\'' +
                ", lon=" + lon +
                ", lat=" + lat +
                ", alt=" + alt +
                ", ground_alt=" + ground_alt +
                ", course=" + course +
                ", pitch=" + pitch +
                ", roll=" + roll +
                ", yaw=" + yaw +
                ", true_airspeed=" + true_airspeed +
                ", ground_speed=" + ground_speed +
                ", remaining_oil=" + remaining_oil +
                ", remaining_dis=" + remaining_dis +
                ", remaining_time=" + remaining_time +
                ", mot_status=" + mot_status +
                ", nav_status=" + nav_status +
                ", com_status=" + com_status +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", wind_speed=" + wind_speed +
                '}';
    }

    public Uav() {
    }
}
