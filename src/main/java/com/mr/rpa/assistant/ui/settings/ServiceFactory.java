package com.mr.rpa.assistant.ui.settings;

import com.google.common.collect.Maps;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.service.impl.SysConfigServiceImpl;
import com.mr.rpa.assistant.service.impl.TaskLogServiceImpl;
import com.mr.rpa.assistant.service.impl.TaskServiceImpl;
import com.mr.rpa.assistant.service.impl.UserServiceImpl;
import org.apache.ibatis.session.SqlSession;

import java.util.Map;

/**
 * Created by feng on 2020/3/27 0027
 */
public class ServiceFactory {

	public static SqlSession session;

	private static Map<Class, Object> serviceMap = Maps.newHashMap();

	public static void init(SqlSession sqlSession) {
		session = sqlSession;
		serviceMap.put(TaskService.class, new TaskServiceImpl(session));
		serviceMap.put(TaskLogService.class, new TaskLogServiceImpl(session));
		serviceMap.put(UserService.class, new UserServiceImpl(session));
		serviceMap.put(SysConfigService.class, new SysConfigServiceImpl(session));
	}

	public static <T> T getService(Class clazz) {
		return (T) serviceMap.get(clazz);
	}
}
