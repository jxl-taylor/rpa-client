package com.mr.rpa.assistant.data.model;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Created by feng on 2020/1/31 0031
 */
@ToString
@Data
public class Task {
	private String id;
	private String name;
	private String mainTask;
	private String desp;
	private String params;
	private String nextTask;
	//是否启动 已开启|未开启
	private boolean running;
	//运行状态 1:运行中 | 0:暂停中
	private int status;
	private int successCount;
	private int failCount;
	private String cron;
	private boolean download;
	private String version;
	private String createBy;
	private String updateBy;
	private Timestamp createTime;
	private Timestamp updateTime;

	public Task() {

	}

	public Task(String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running,
				Integer status, String cron, String createBy) {
		this(id, name, mainTask, desp, params, nextTask, running, status, cron, 0, 0, createBy);
	}

	public Task(String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running,
				Integer status, String cron,
				Integer successCount, Integer failCount, String createBy) {
		this(id, name, mainTask, desp, params, nextTask, running, status, cron, false, "",
				successCount, failCount, createBy);
	}

	public Task(String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running,
				Integer status, String cron, Boolean download, String version,
				Integer successCount, Integer failCount, String createBy) {
		this(id, name, mainTask, desp, params, nextTask, running, status,
				cron, download, version, successCount, failCount, createBy, new Timestamp(System.currentTimeMillis()),
				new Timestamp(System.currentTimeMillis()));
	}

	public Task(String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running,
				Integer status, String cron, Boolean download, String version,
				Integer successCount, Integer failCount, String createBy, Timestamp createTime, Timestamp updateTime) {
		this.id = id;
		this.name = name;
		this.mainTask = mainTask;
		this.desp = desp;
		this.params = params;
		this.nextTask = nextTask;
		this.running = running;
		this.status = status;
		this.successCount = successCount;
		this.failCount = failCount;
		this.cron = cron;
		this.download = download;
		this.version = version;
		this.createBy = createBy;
		this.updateBy = createBy;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}

}
