package com.mr.rpa.assistant.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class KettleQuartzJob implements Job {
	private static Logger logger= Logger.getLogger(KettleQuartzJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("=========================start job =============================");



		logger.info("=========================do something =============================");



		logger.info("=========================start job =============================");
	}

}
