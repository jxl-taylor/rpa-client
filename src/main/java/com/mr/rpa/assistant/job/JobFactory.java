package com.mr.rpa.assistant.job;

import cn.hutool.core.collection.CollectionUtil;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.LogTextCollector;
import com.mr.rpa.assistant.util.SystemContants;
import lombok.extern.log4j.Log4j;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 2020/2/23 0023
 */
@Log4j
public class JobFactory implements Runnable {

	private static boolean isStarted = false;

	private static JobFactory jobFactory = new JobFactory();

	private static ExecutorService executor = Executors.newFixedThreadPool(2, new JobThreadFactory());

	private JobFactory() {
	}

	private Scheduler userScheduler;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	/**
	 * 开启定时任务
	 *
	 * @throws SchedulerException
	 */
	public static void start() throws SchedulerException, KettleException {
		if (!isStarted) {
			//初始化
			KettleEnvironment.init();
			KettleLogStore.getAppender().addLoggingEventListener(new KettleLoggingEventListener() {
				@Override
				public void eventAdded(KettleLoggingEvent kettleLoggingEvent) {
					GlobalProperty globalProperty = GlobalProperty.getInstance();
					LogTextCollector textCollector = globalProperty.getLogTextCollector();
					textCollector.addLog(kettleLoggingEvent.getMessage().toString());
					globalProperty.getLogShows().forEach(item -> {
						if (item != null) item.scrollText();
					});
				}
			});

			Thread thread = new Thread(() -> {
				executor.submit(jobFactory);

			});
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * 退出定时任务
	 *
	 * @throws SchedulerException
	 */
	public static void exit() throws SchedulerException {
		if (isStarted) {
			executor.shutdownNow();
		}
	}

	/**
	 * taskLogId
	 *
	 * @param taskLog
	 * @throws SchedulerException
	 */
	public static void trigger(TaskLog taskLog) throws SchedulerException {
		scheduleAble();
		Task task = jobFactory.taskDao.queryTaskById(taskLog.getTaskId());
		String taskId = task.getId();

		JobDataMap map = new JobDataMap();
		map.put(SystemContants.TASK_LOG_KEY, taskLog);
		jobFactory.userScheduler.triggerJob(JobKey.jobKey(taskId), map);
	}

	public static void add(Task task) throws SchedulerException {
		scheduleAble();
		String taskId = task.getId();
		JobDetail jobDetail = JobBuilder.newJob(KettleQuartzJob.class).
				withIdentity(taskId)
				.build();
		Trigger jobTrigger = TriggerBuilder.newTrigger().withIdentity(taskId)
				.withSchedule(CronScheduleBuilder.cronSchedule(task.getCron())).build();
		if (task.isRunning()) {
			jobFactory.userScheduler.scheduleJob(jobDetail, jobTrigger);
			if (task.getStatus() == SystemContants.TASK_RUNNING_STATUS_PAUSE) pause(task);
		}
	}

	public static void delete(Task task) throws SchedulerException {
		scheduleAble();
		String taskId = task.getId();
		jobFactory.userScheduler.deleteJob(JobKey.jobKey(taskId));
	}

	public static void update(Task task) throws SchedulerException {
		scheduleAble();
		delete(task);
		//todo 这个需要确保成功
		add(task);
	}

	public static void pauseAll() throws SchedulerException {
		scheduleAble();
		jobFactory.userScheduler.pauseAll();
	}

	public static void pause(Task task) throws SchedulerException {
		scheduleAble();
		String taskId = task.getId();
		jobFactory.userScheduler.pauseJob(JobKey.jobKey(taskId));
	}

	public static void resumeAll() throws SchedulerException {
		scheduleAble();
		jobFactory.userScheduler.resumeAll();
	}

	public static void resume(Task task) throws SchedulerException {
		scheduleAble();
		String taskId = task.getId();
		jobFactory.userScheduler.resumeJob(JobKey.jobKey(taskId));
	}

	public static void triggerByManual(String taskId) throws SchedulerException {
		executor.submit(()-> new KettleQuartzJob().triggerByManual(taskId));
	}

	static class JobThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		JobThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" +
					poolNumber.getAndIncrement() +
					"-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r,
					namePrefix + threadNumber.getAndIncrement(),
					0);
			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	@Override
	public void run() {
		try {
			userScheduler = new StdSchedulerFactory().getScheduler();
			userScheduler.start();
			//遍历任务表，将任务开启
			if (CollectionUtil.isEmpty(taskDao.getTaskList())) taskDao.loadTaskList();
			for (Task task : taskDao.queryTaskList()) {
				if (task.isRunning()) {
					JobDetail jobDetail = JobBuilder.newJob(KettleQuartzJob.class).
							withIdentity(task.getId())
							.build();
					Trigger jobTrigger = TriggerBuilder.newTrigger().withIdentity(task.getId())
							.withSchedule(CronScheduleBuilder.cronSchedule(task.getCron())).build();
					userScheduler.scheduleJob(jobDetail, jobTrigger);
					log.info(String.format("JOB 添加并启动，TASK=[%s]", task));
					//暂停的任务
					if (task.getStatus() == SystemContants.TASK_RUNNING_STATUS_PAUSE) {
						userScheduler.pauseJob(JobKey.jobKey(task.getId()));
						log.info(String.format("JOB 添加并暂停，TASK=[%s]", task));
					}
				}
			}
			isStarted = true;
			log.info("调度器开启成功");
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException("启动定时任务失败");
		}
	}

	private static void scheduleAble() throws SchedulerException {
		if (!isStarted) {
			throw new SchedulerException("调度器未启动");
		}
	}

}
