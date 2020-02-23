package com.mr.rpa.assistant.data.model;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by feng on 2020/2/23 0023
 */
@Data
public class TaskErrorLog {

	private String id;

	private String taskLogId;

	private String  error;

	private Timestamp createTime;

}
