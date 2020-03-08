package com.mr.rpa.assistant.job;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.KeyValue;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.job.JobMeta;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Log4j
public class KettleQuartzJob implements Job {

	private TaskDao taskDao = GlobalProperty.applicationContext.getBean(TaskDao.class);

	private TaskLogDao taskLogDao = GlobalProperty.applicationContext.getBean(TaskLogDao.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String taskId = context.getJobDetail().getKey().getName();
		Task task = taskDao.queryTaskById(taskId);
		JobDataMap dataMap = context.getTrigger().getJobDataMap();
		TaskLog taskLog = (TaskLog) dataMap.get(SystemContants.TASK_LOG_KEY);
		try {
			if (taskLog == null) {
				taskLog = addLog(taskId);
			}

			log.info(String.format("taskId=[%s], taskLogId=[%s]: job run start", task.getName(), taskLog.getId()));

			runKbj(task.getName(), task.getMainTask(), JSON.parseArray(task.getParams()).toJavaList(KeyValue.class));
			//执行依赖任务
			if(StringUtils.isNotBlank(task.getNextTask())){
				Task nextTask = taskDao.queryTaskByName(task.getNextTask());
				runKbj(nextTask.getName(), nextTask.getMainTask(), JSON.parseArray(nextTask.getParams()).toJavaList(KeyValue.class));
			}
			taskLog.setStatus(SystemContants.TASK_LOG_STATUS_SUCCESS);
			taskLogDao.updateTaskLog(taskLog);
			log.info(String.format("taskId=[%s], taskLogId=[%s]: job run end", task.getName(), taskLog.getId()));
		} catch (Exception e) {
			if (taskLog != null) {
				taskLog.setError(e.getMessage());
				taskLog.setStatus(SystemContants.TASK_LOG_STATUS_FAIL);
				taskLogDao.updateTaskLog(taskLog);
			}
			log.error(e);
		}
	}

	private TaskLog addLog(String taskId) {
		TaskLog taskLog = new TaskLog(UUID.randomUUID().toString(), taskId,
				SystemContants.TASK_LOG_STATUS_RUNNING, "",
				new Timestamp(System.currentTimeMillis()), null);
		taskLogDao.insertNewTaskLog(taskLog);
		return taskLog;
	}

	protected void runKbj(String taskName, String kbjName, List<KeyValue> kvList) throws Exception {
		String taskFileDir = GlobalProperty.applicationContext.getBean(SysConfig.class).getTaskFilePath()
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
