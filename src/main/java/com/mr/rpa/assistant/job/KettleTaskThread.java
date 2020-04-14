package com.mr.rpa.assistant.job;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.KeyValue;
import com.mr.rpa.assistant.util.Pair;
import com.mr.rpa.assistant.util.SystemContants;
import com.mr.rpa.assistant.util.email.EmailUtil;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.job.JobMeta;
import org.quartz.JobDataMap;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by feng on 2020/4/14 0014
 */
@Log4j
public class KettleTaskThread extends Thread {

	private static boolean isStarted = false;
	private static KettleTaskThread taskThread = new KettleTaskThread();
	private LinkedBlockingQueue<Pair<String, JobDataMap>> taskQueue = GlobalProperty.getInstance().getTaskQueue();

	private TaskService taskService = ServiceFactory.getService(TaskService.class);
	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	private KettleTaskThread() {
		super();
		setDaemon(true);
	}

	public static void startProcessTask() {
		if(!isStarted) taskThread.start();
	}

	@Override
	public void run() {
		while (true) {
			Task task = null;
			TaskLog taskLog = null;
			try {
				Pair<String, JobDataMap> taskPair = taskQueue.take();
				task = taskService.queryTaskById(taskPair.getObject1());
				JobDataMap dataMap = taskPair.getObject2();
				if (dataMap != null) taskLog = (TaskLog) dataMap.get(SystemContants.TASK_LOG_KEY);

				if (taskLog == null) {
					taskLog = addLog(task.getId());
				}

				log.info(String.format("taskId=[%s], taskLogId=[%s]: job run start", task.getName(), taskLog.getId()));

				runKbj(task.getName(), task.getMainTask(), JSON.parseArray(task.getParams()).toJavaList(KeyValue.class));
				//执行依赖任务
				if (StringUtils.isNotBlank(task.getNextTask())) {
					Task nextTask = taskService.queryTaskByName(task.getNextTask());
					runKbj(nextTask.getName(), nextTask.getMainTask(), JSON.parseArray(nextTask.getParams()).toJavaList(KeyValue.class));
				}
				taskLog.setStatus(SystemContants.TASK_LOG_STATUS_SUCCESS);
				taskLogService.updateTaskLog(taskLog);
				log.info(String.format("taskId=[%s], taskLogId=[%s]: job run end", task.getName(), taskLog.getId()));
			} catch (Throwable e) {
				log.error(e);
				if (taskLog != null) {
					taskLog.setError(e.getMessage());
					taskLog.setStatus(SystemContants.TASK_LOG_STATUS_FAIL);
					taskLogService.updateTaskLog(taskLog);
				}
				SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
				if (StringUtils.isNotBlank(sysConfig.getToMails()))
					EmailUtil.sendBgMail(sysConfig.getToMails(),
							String.format("BOT[%s]运行失败。\n\n原因：%s", task.toString(), e.getMessage()),
							String.format("BOT[%s]运行失败", task.getName()));
			}
		}
	}

	private TaskLog addLog(String taskId) {
		TaskLog taskLog = new TaskLog(UUID.randomUUID().toString(), taskId,
				SystemContants.TASK_LOG_STATUS_RUNNING, "",
				new Timestamp(System.currentTimeMillis()), null);
		taskLogService.insertNewTaskLog(taskLog);
		return taskLog;
	}

	protected void runKbj(String taskName, String kbjName, List<KeyValue> kvList) throws Exception {
		if (!LicenseManagerHolder.getLicenseManagerHolder().verifyCert())
			throw new RuntimeException("License已失效");

		String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath()
				+ File.separator + taskName;
		FileUtil.mkdir(taskFileDir);
		String jobPath = taskFileDir + File.separator + kbjName;
		if (!FileUtil.exist(jobPath)) throw new RuntimeException(kbjName + "不存在");
		// jobname 是Job脚本的路径及名称
		JobMeta jobMeta = new JobMeta(jobPath, null);
		org.pentaho.di.job.Job job = new org.pentaho.di.job.Job(null, jobMeta);
		// 向Job 脚本传递参数，脚本中获取参数值：${参数名}  ,此参数可有可无，按需加入
		kvList.forEach(keyValue -> job.setVariable(keyValue.getObject1(), keyValue.getObject2()));
		job.start();
		job.waitUntilFinished();
		if (job.getErrors() > 0) {
			throw new Exception("There are errors during job exception!(执行job发生异常)");
		}

	}
}
