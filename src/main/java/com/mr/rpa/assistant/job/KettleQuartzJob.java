package com.mr.rpa.assistant.job;

import cn.hutool.core.io.FileUtil;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.derby.client.am.LogWriter;
import org.apache.log4j.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.JobMeta;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;

@Log4j
public class KettleQuartzJob implements Job {

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

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

			runKbj(task.getName());
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

	protected void runKbj(String kbjName) {
		String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath();
		FileUtil.mkdir(taskFileDir);
		String jobPath = taskFileDir + File.separator + kbjName;
		if (!FileUtil.exist(jobPath)) throw new RuntimeException(kbjName + "不存在");
		try {
			// jobname 是Job脚本的路径及名称
			JobMeta jobMeta = new JobMeta(jobPath, null);
			org.pentaho.di.job.Job job = new org.pentaho.di.job.Job(null, jobMeta);
			// 向Job 脚本传递参数，脚本中获取参数值：${参数名}  ,此参数可有可无，按需加入
			// job.setVariable(paraname, paravalue);
//			job.setVariable("id", params[0]);
//			job.setVariable("content", params[1]);
//			job.setVariable("file", params[2]);
			job.start();
			job.waitUntilFinished();
			if (job.getErrors() > 0) {
				throw new Exception(
						"There are errors during job exception!(执行job发生异常)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
