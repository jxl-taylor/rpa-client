package com.mr.rpa.assistant.service;

import com.mr.rpa.assistant.data.model.SysConfig;

/**
 * Created by feng on 2020/3/26 0026
 */
public interface SysConfigService {

	SysConfig query();

	void insert();

	void update();
}
