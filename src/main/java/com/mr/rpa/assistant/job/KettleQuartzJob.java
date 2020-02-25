package com.mr.rpa.assistant.job;

import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
			if(taskLog == null){
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
		//todo
		log.info("=========================do something =============================");

	}

}
