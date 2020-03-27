package com.mr.rpa.assistant.service.impl;

import com.mr.rpa.assistant.dao.SysConfigMapper;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by feng on 2020/3/27 0027
 */
public class SysConfigServiceImpl implements SysConfigService {

	private SysConfigMapper sysConfigMapper;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	public SysConfigServiceImpl(SqlSession session) {
		sysConfigMapper = session.getMapper(SysConfigMapper.class);
	}

	@Override
	public SysConfig query() {
		return sysConfigMapper.query();
	}

	@Override
	public void insert() {
		sysConfigMapper.insert(globalProperty.getSysConfig());
	}

	@Override
	public void update() {
		sysConfigMapper.delete();
		insert();
	}
}
