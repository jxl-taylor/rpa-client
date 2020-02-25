package com.mr.rpa.assistant.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by feng on 2020/1/31 0031
 */
@Data
@AllArgsConstructor
public class TaskLog {
	private String id;
	private String taskId;
	//0：正在执行	1：执行成功	2：执行失败
	private  Integer status;
	private String error;
	private Timestamp startTime;
	private  Timestamp endTime;

	public TaskLog(){
	}
}
