package com.mr.rpa.assistant.data.model;

import com.mr.rpa.assistant.util.SystemContants;
import lombok.Data;

import java.io.File;

/**
 * Created by feng on 2020/2/5 0005
 */
@Data
public class SysConfig {

	private static String DEFAULT_BOT_FILE_DIR = System.getProperty("user.dir");

	private String adminUsername;
	private String adminPassword;
	private String mailServerName;
	private Integer mailSmtpPort = 25;
	private String mailEmailAddress;
	private String mailEmailPassword;
	private Boolean mailSslCheckbox = false;
	private String taskFilePath = DEFAULT_BOT_FILE_DIR + File.separator + "mbot";
	private String logPath = DEFAULT_BOT_FILE_DIR + File.separator + "log";
	private String controlServer;
	private String dbPath = DEFAULT_BOT_FILE_DIR + File.separator + "database";
	private String updatePath = DEFAULT_BOT_FILE_DIR + File.separator + "update";
	private Integer miniteErrorLimit = 10;
	private Integer runningLimit = 100;

	public String getBotRootDir(){
		return DEFAULT_BOT_FILE_DIR;
	}
}
