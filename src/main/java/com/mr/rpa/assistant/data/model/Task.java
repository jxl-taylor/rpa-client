package com.mr.rpa.assistant.data.model;

import com.mr.rpa.assistant.util.SystemContants;
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
	private String desp;
	//是否启动 已开启|未开启
	private boolean running;
	//运行状态 运行中|暂停中
	private Integer status;
	private Integer successCount;
	private Integer failCount;
	private String cron;
	private Timestamp createTime;
	private Timestamp updateTime;

	public Task() {

	}

	public Task(String id, String name, String desp, Boolean running,
				Integer status, String cron) {
		this(id, name, desp, running, status, cron, 0, 0);
	}

	public Task(String id, String name, String desp, Boolean running,
				Integer status, String cron,
				Integer successCount, Integer failCount) {
		this(id, name, desp, running, status, cron, successCount, failCount, null, new Timestamp(System.currentTimeMillis()));
	}

	public Task(String id, String name, String desp, Boolean running,
				Integer status, String cron,
				Integer successCount, Integer failCount, Timestamp createTime, Timestamp updateTime) {
		this.id = id;
		this.name = name;
		this.desp = desp;
		this.running = running;
		this.status = status;
		this.successCount = successCount;
		this.failCount = failCount;
		this.cron = cron;
		this.createTime = createTime;
		this.updateTime = updateTime;
	}
}
