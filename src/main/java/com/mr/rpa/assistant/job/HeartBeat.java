package com.mr.rpa.assistant.job;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 2020/3/16 0016
 */
@Log4j
public class HeartBeat implements Runnable {
	private GlobalProperty globalProperty = GlobalProperty.getInstance();
	private final static String SERVICE_ID_HEARTBEAT = "heartbeat";
	private SysConfig sysConfig;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	private Map<String, String> operaErrorMsgMap = Maps.newHashMap();

	@Override
	public void run() {
		sysConfig = globalProperty.getSysConfig();
		while (true) {
			try {
				Thread.sleep(SystemContants.HEARTBEAT_INTERVAL);
				action();
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
			if (StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(null);
			return false;
		}
		LinkedHashMap<String, Object> jsonMap = Maps.newLinkedHashMap();
		jsonMap.put("cpuLoad", CommonUtil.getCpuLoad());
		jsonMap.put("freeMemory", CommonUtil.getFreeMemory());
		jsonMap.put("totalMemory", CommonUtil.getTotalMemory());
		jsonMap.put("freeDisk", CommonUtil.getFreeDisk());

		List<LinkedHashMap<String, Object>> botContentList = Lists.newArrayList();
		taskService.queryTaskList().forEach(task -> {
			LinkedHashMap<String, Object> taskMap = Maps.newLinkedHashMap();
			taskMap.put("botName", task.getName());
			taskMap.put("mainBot", task.getMainTask());
			taskMap.put("botStatus", task.isRunning() && task.getStatus() == SystemContants.TASK_RUNNING_STATUS_RUN);
			taskMap.put("runningStatus", CollectionUtils.isEmpty(taskLogService.loadTaskLogByTaskId(task.getId())) ?
					SystemContants.TASK_LOG_STATUS_UNKNOWN : taskLogService.loadTaskLogByTaskId(task.getId()).get(0).getStatus()
			);
			taskMap.put("successCount", task.getSuccessCount());
			taskMap.put("failCount", task.getFailCount());
			taskMap.put("operaErrorMsg", operaErrorMsgMap.getOrDefault(task.getName(), ""));
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
		if (resultJson == null) throw new RuntimeException(String.format("[%s]控制中心地址无法识别", controlUrl));
		String resultCode = resultJson.getString("resultcode");
		if (resultCode != null && resultCode.equals(SystemContants.API_SUCCESS)) {
			//清空上一次操作的信息
			operaErrorMsgMap.clear();
			if (sysConfig.getConnectTime() == null) {
				if (StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(new java.util.Date());
			}

			//处理控制中心操作请求
			JSONArray jsonArray = resultJson.getJSONArray("botContent");
			if (jsonArray != null && jsonArray.size() > 0) {
				handleAction(jsonArray);
			}
			return true;
		}
		if (StringUtils.isBlank(controlUrl)) sysConfig.setConnectTime(null);
		log.error(resultJson.getString("message"));

		return false;
	}

	private void handleAction(JSONArray jsonArray) {
		TaskListController controller = GlobalProperty.getInstance().getTaskListController();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String operation = jsonObject.getString("operation");
			String name = jsonObject.getString("botName");
			try {
				Task task = taskService.queryTaskByName(name);
				if (operation.equals(SystemContants.API_TASK_OPERA_START)) {
					controller.startTask(task);
				} else if (operation.equals(SystemContants.API_TASK_OPERA_STOP)) {
					controller.endTask(task);
				} else if (operation.equals(SystemContants.API_TASK_OPERA_RESUME)) {
					controller.resumeTask(task);
				} else if (operation.equals(SystemContants.API_TASK_OPERA_PAUSE)) {
					controller.pauseTask(task);
				} else if (operation.equals(SystemContants.API_TASK_OPERA_TRIGGER)) {
					controller.triggerByManual(task);
				} else if (operation.equals(SystemContants.API_TASK_OPERA_CLEARLOG)) {
					controller.deleteAllTaskLog(task);
				}
			} catch (Throwable e) {
				operaErrorMsgMap.put(jsonObject.getString("botName"),
						String.format("操作编号：%s, 错误原因：%s", operation, e.getMessage()));
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(CommonUtil.getFreeDisk());
	}

}
