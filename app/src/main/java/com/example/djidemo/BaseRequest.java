package com.example.djidemo;

public class BaseRequest {
    /** START：开始任务 END：结束任务 UAVDATA：实时飞行数据 */
    private String action;

    /** 飞机编号 */

    private String uav_no;

    public BaseRequest(String action, String uav_no) {
        this.action = action;
        this.uav_no = uav_no;
    }

    public BaseRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUav_no() {
        return uav_no;
    }

    public void setUav_no(String uav_no) {
        this.uav_no = uav_no;
    }

    @Override
    public String toString() {
        return "BaseRequest{" +
                "action='" + action + '\'' +
                ", uav_no='" + uav_no + '\'' +
                '}';
    }
}
