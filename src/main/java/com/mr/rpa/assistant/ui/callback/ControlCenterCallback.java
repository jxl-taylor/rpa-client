package com.mr.rpa.assistant.ui.callback;

import com.alibaba.fastjson.JSONObject;

/**
 * @author feng
 */
public interface ControlCenterCallback<T> {

    public T processReturn(JSONObject resultJson) throws Exception;
}
