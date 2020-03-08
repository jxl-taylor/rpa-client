package com.mr.rpa.assistant.data.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.File;

/**
 * Created by feng on 2020/2/5 0005
 */
@Data
@Component("sysConfig")
public class SysConfig {

	private static String DEFAULT_BOT_FILE_DIR = System.getProperty("user.dir");

	private String adminUsername;
	private String adminPassword;
	private String mailServerName;
	private Integer mailSmtpPort = 25;
	private String mailEmailAddress;
	private String mailEmailPassword;
	private Boolean mailSslCheckbox = false;
	private String taskFilePath = DEFAULT_BOT_FILE_DIR + File.separator + "kjb";
	private String logPath = DEFAULT_BOT_FILE_DIR + File.separator + "log";
	private String controlServer;
	private String dbPath = DEFAULT_BOT_FILE_DIR + File.separator + "database";
	private Integer miniteErrorLimit = 10;
	private Integer runningLimit = 100;

}
