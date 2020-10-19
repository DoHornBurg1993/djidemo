package com.ast.djisdk.http;

/**
 * @author： DuHongBo
 * 请求回调接口
 */

public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
