package com.mr.rpa.assistant.ui.callback;

import com.alibaba.fastjson.JSONObject;

/**
 * @author feng
 */
public interface ControlCenterCallback {
    
    public void processReturn(JSONObject resultJson);
}
