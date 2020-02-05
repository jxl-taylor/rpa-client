package com.mr.rpa.assistant.data.model;

import lombok.Data;

/**
 * Created by feng on 2020/2/5 0005
 */
@Data
public class SysConfig {

	private String adminUsername;
	private String adminPassword;
	private String mailServerName;
	private Integer mailSmtpPort = 25;
	private String mailEmailAddress;
	private String mailEmailPassword;
	private Boolean mailSslCheckbox = false;
	private String taskFilePath = "D:/MR-ROBOT/kjb";
	private String logPath = "D:/MR-ROBOT/log";
	private String controlServer;
	private String dbPath = "D:/MR-ROBOT/database";
	private Integer miniteErrorLimit = 10;
	private Integer runningLimit = 100;

}
