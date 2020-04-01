package com.mr.rpa.assistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mr.rpa.assistant.dao.SysConfigMapper;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

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
		List<Map<String, Object>> sysConfigList = sysConfigMapper.queryList();
		if (CollectionUtil.isNotEmpty(sysConfigList)) {
			Map<String, Object> map = sysConfigList.get(0);
			SysConfig sysConfig = new SysConfig();
			sysConfig.setControlServer(String.valueOf(map.get("control_server".toUpperCase())));
			sysConfig.setMailServerName(String.valueOf(map.get("mail_server_name".toUpperCase())));
			sysConfig.setMailSmtpPort((Integer) map.get("mail_server_port".toUpperCase()));
			sysConfig.setMailEmailAddress(String.valueOf(map.get("mail_user_email".toUpperCase())));
			sysConfig.setMailEmailPassword(String.valueOf(map.get("mail_user_password".toUpperCase())));
			sysConfig.setMailSslCheckbox((Boolean) map.get("mail_ssl_enabled".toUpperCase()));
			sysConfig.setToMails(String.valueOf(map.get("to_mails".toUpperCase())));
			return sysConfig;
		}
		return null;
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
