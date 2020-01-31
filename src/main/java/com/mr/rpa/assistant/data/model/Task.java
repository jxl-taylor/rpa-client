package com.mr.rpa.assistant.data.model;

import lombok.Data;

/**
 * Created by feng on 2020/1/31 0031
 */
@Data
public class Task {
	private String id;
	private String name;
	private String desp;
	private  Boolean running;
	private  Integer status;
	private  Integer successCount;
	private  Integer failCount;

	public Task(String id, String name, String desp, Boolean running, Integer status, Integer successCount, Integer failCount) {
		this.id = id;
		this.name = name;
		this.desp = desp;
		this.running = running;
		this.status = status;
		this.successCount = successCount;
		this.failCount = failCount;
	}
}
