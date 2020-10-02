package com.example.djidemo.http;

/**
 * @author： DuHongBo
 */
public class Uav {
    /** 0:就绪(起飞前检查完成),1:起飞中(从解锁到起飞航线执行完),2:任务中(在任务航线列表中),3.	飞行中，4:降落中(进降落航线加锁前),5:已着陆(已加锁) */
    private int flyStatus;

    /** 当前时间 */
    private String time;

    /** 飞机经度 */
    private Double lon;

    /** 飞机纬度 */
    private Double lat;

    /** 飞机海拔高度,单位米 */
    private Double alt;

    /** 飞行场高,单位米 */
    private Double groundAlt;

    /** 飞机航向角 */
    private Double course;

    /** 飞机俯仰角 */
    private Double pitch;

    /** 飞机横滚角 */
    private Double roll;

    /** 飞机偏航角 */
    private Double yaw;

    /** 飞机真空速,km/h */
    private Double trueAirspeed;

    /** 飞机地速,km/h */
    private Double groundSpeed;

    /** 剩余电量,百分比,80.0为80% */
    private Double remainingOil;

    /** 最大剩余航程,单位千米 */
    private Double remainingDis;

    /** 最大留空时间,分钟 */
    private Double remainingTime;

    /** 动力系统状态0正常,1故障 */
    private Integer motStatus;

    /** 导航系统状态0正常,1故障 */
    private Integer navStatus;

    /** 通信系统状态0正常,1故障 */
    private Integer comStatus;

    /** 温度,摄氏度 */
    private Double temperature;

    /** 湿度，百分比 */
    private Double humidity;

    /** 风速,m/s */
    private Double windSpeed;

    //用于URL传参和取参时的key

    public static String FLYSTATUS = "flyStatus";
    public static String TIME = "time";
    public static String LON = "lon";
    public static String LAT = "lat";
    public static String ALT = "alt";
    public static String GROUNDALT = "groundAlt";
    public static String COURSE = "course";
    public static String PITCH = "pitch";
    public static String ROLL = "roll";
    public static String YAW = "yaw";
    public static String TRUEAIRSPEED = "trueAirspeed";
    public static String GROUNDSPEED = "groundSpeed";
    public static String REMAININGOIL = "remainingOil";
    public static String REMAININGDIS = "remainingDis";
    public static String REMAININGTIME = "remainingTime";
    public static String MOTSTATUS = "motStatus";
    public static String NAVSTATUS = "navStatus";
    public static String COMSTATUS = "comStatus";
    public static String TEMPERATURE = "temperature";
    public static String HUMIDITY = "humidity";
    public static String WINDSPEED= "windSpeed";

    public int getFlyStatus() {
        return flyStatus;
    }

    public void setFlyStatus(int flyStatus) {
        this.flyStatus = flyStatus;
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

    public Double getGroundAlt() {
        return groundAlt;
    }

    public void setGroundAlt(Double groundAlt) {
        this.groundAlt = groundAlt;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
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

    public Double getTrueAirspeed() {
        return trueAirspeed;
    }

    public void setTrueAirspeed(Double trueAirspeed) {
        this.trueAirspeed = trueAirspeed;
    }

    public Double getGroundSpeed() {
        return groundSpeed;
    }

    public void setGroundSpeed(Double groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public Double getRemainingOil() {
        return remainingOil;
    }

    public void setRemainingOil(Double remainingOil) {
        this.remainingOil = remainingOil;
    }

    public Double getRemainingDis() {
        return remainingDis;
    }

    public void setRemainingDis(Double remainingDis) {
        this.remainingDis = remainingDis;
    }

    public Double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public Integer getMotStatus() {
        return motStatus;
    }

    public void setMotStatus(Integer motStatus) {
        this.motStatus = motStatus;
    }

    public Integer getNavStatus() {
        return navStatus;
    }

    public void setNavStatus(Integer navStatus) {
        this.navStatus = navStatus;
    }

    public Integer getComStatus() {
        return comStatus;
    }

    public void setComStatus(Integer comStatus) {
        this.comStatus = comStatus;
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

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Uav(int flyStatus, String time, Double lon, Double lat, Double alt, Double groundAlt, Double course, Double pitch, Double roll, Double yaw, Double trueAirspeed, Double groundSpeed, Double remainingOil, Double remainingDis, Double remainingTime, Integer motStatus, Integer navStatus, Integer comStatus, Double temperature, Double humidity, Double windSpeed) {
        this.flyStatus = flyStatus;
        this.time = time;
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
        this.groundAlt = groundAlt;
        this.course = course;
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
        this.trueAirspeed = trueAirspeed;
        this.groundSpeed = groundSpeed;
        this.remainingOil = remainingOil;
        this.remainingDis = remainingDis;
        this.remainingTime = remainingTime;
        this.motStatus = motStatus;
        this.navStatus = navStatus;
        this.comStatus = comStatus;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public Uav() {
    }
}
