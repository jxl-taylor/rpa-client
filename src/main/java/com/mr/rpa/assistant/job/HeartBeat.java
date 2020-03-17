package com.mr.rpa.assistant.job;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by feng on 2020/3/16 0016
 */
@Log4j
public class HeartBeat implements Runnable {
	private GlobalProperty globalProperty = GlobalProperty.getInstance();
	private final static String SERVICE_ID_HEARTBEAT = "heartbeat";
	private SysConfig sysConfig;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void run() {
		sysConfig = globalProperty.getSysConfig();
		while (true) {
			try {
				action();
				Thread.sleep(SystemContants.HEARTBEAT_INTERVAL);
			} catch (Exception e) {
				sysConfig.setConnectTime(null);
				log.error(e);
			}
		}

	}

	private boolean action() throws Exception {
		return action(null);
	}

	public boolean action(String controlUrl) throws Exception {
		String url = "";
		sysConfig = globalProperty.getSysConfig();
		if (StringUtils.isBlank(controlUrl)) {
			url = sysConfig.getControlServer();
		} else {
			url = controlUrl;
		}

		if (StringUtils.isBlank(url)) {
			if(StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(null);
			return false;
		}
		try{
			LinkedHashMap<String, Object> jsonMap = Maps.newLinkedHashMap();
			jsonMap.put("cpuLoad", CommonUtil.getCpuLoad());
			jsonMap.put("freeMemory", CommonUtil.getFreeMemory());
			jsonMap.put("totalMemory", CommonUtil.getTotalMemory());
			jsonMap.put("freeDisk", CommonUtil.getFreeDisk());

			List<LinkedHashMap<String, Object>> botContentList = Lists.newArrayList();
			taskDao.queryTaskList().forEach(task -> {
				LinkedHashMap<String, Object> taskMap = Maps.newLinkedHashMap();
				taskMap.put("botName", task.getName());
				taskMap.put("mainBot", task.getMainTask());
				taskMap.put("botStatus", task.isRunning() && task.getStatus() == SystemContants.TASK_RUNNING_STATUS_RUN);
				taskMap.put("runningStatus", CollectionUtils.isEmpty(taskLogDao.loadTaskLogList(task.getId())) ?
						SystemContants.TASK_LOG_STATUS_RUNNING : taskLogDao.loadTaskLogList(task.getId()).get(0).getStatus()
				);
				botContentList.add(taskMap);
			});

			jsonMap.put("botContent", botContentList);

			String result = HttpRequest.post(url)
					.header("serviceId", SERVICE_ID_HEARTBEAT)
					.header("clientVersion", SystemContants.CLIENT_VERSION_1_0)
					.header("privateKey", SystemContants.PRIVATE_KEY)
					.header("mac", CommonUtil.getLocalMac())
					.header("processId", CommonUtil.getProcessID())
					.body(JSON.toJSONString(jsonMap))
					.execute().body();
			JSONObject resultJson = JSON.parseObject(result);
			if(resultJson == null) throw new RuntimeException(String.format("[%s]控制中心地址无法识别", controlUrl));
			String resultCode = resultJson.getString("resultcode");
			if (resultCode != null && resultCode.equals(SystemContants.API_SUCCESS)) {
				if (sysConfig.getConnectTime() == null) {
					if(StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(new java.util.Date());
				}
				return true;
			}
			if(StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(null);
			log.error(resultJson.getString("message"));
		}catch (Exception e){
			e.printStackTrace();
			throw e;
		}

		return false;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(CommonUtil.getFreeDisk());
	}

}
