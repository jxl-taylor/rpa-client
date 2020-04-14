package com.mr.rpa.assistant.job;

import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.Pair;
import lombok.extern.log4j.Log4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


@Log4j
public class KettleQuartzJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String taskId = context.getJobDetail().getKey().getName();
		JobDataMap dataMap = context.getTrigger().getJobDataMap();
		GlobalProperty.getInstance().getTaskQueue().offer(new Pair<>(taskId, dataMap));
	}
}
