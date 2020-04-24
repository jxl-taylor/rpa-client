package com.mr.rpa.assistant.data.model;

import cn.hutool.core.io.FileUtil;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by feng on 2020/2/5 0005
 */
@Data
@Log4j
public class SysConfig {

	//	private static String DEFAULT_BOT_FILE_DIR = System.getProperty("user.dir");
	private static String BOT_FILE_DIR_NAME = "recfile";
	private static String DEFAULT_BOT_CONTROL_SERVER = "https://api.rpalinker.com";

	private static String BOT_FILE_DIR = System.getProperty("user.dir") + File.separator + BOT_FILE_DIR_NAME;

	private String id = "1";
	private String adminUsername = "admin";
	private String adminPassword = "admin";
	private String adminNick = "系统管理员";
	private String mailServerName = "";
	private Integer mailSmtpPort = 25;
	private String mailEmailAddress = "";
	private String mailEmailPassword = "";
	private String toMails = "";
	private Boolean mailSslCheckbox = false;
	private String taskFilePath = BOT_FILE_DIR + File.separator + "mbot";
	private String taskFilePathTmp = taskFilePath + File.separator + "tmp";
	private String logPath = BOT_FILE_DIR + File.separator + "log";
	private String controlServer = DEFAULT_BOT_CONTROL_SERVER;
	//注册信息，通过控制中心查询获得，无连接或者没有注册则无此信息
	private String userType = "";
	private String companyName = "";
	private String companyAddress = "";
	private String applicant = "";
	private String applyPhone1 = "";
	private String applyPhone2 = "";
	private String applyMail = "";

	private java.util.Date connectTime;
	private String dbPath = BOT_FILE_DIR_NAME + File.separator + "database";
	private String updatePath = BOT_FILE_DIR + File.separator + "update";
	private String CONFIG_FILE = BOT_FILE_DIR + File.separator + "config.txt";
	private String runResultPath = BOT_FILE_DIR + File.separator
			+ "source" + File.separator
			+ "bank" + File.separator
			+ "%s" + File.separator
			+ "data";
	private String defaultResultPath = BOT_FILE_DIR + File.separator + "source";
	private Integer miniteErrorLimit = 10;
	private Integer runningLimit = 100;

	public String getBotRootDir() {
		return System.getProperty("user.dir");
	}

	public User getAdminUser() {
		Gson gson = new Gson();
		User user = new User();
		user.setUsername(adminUsername);
		user.setPassword(adminPassword);
		try {
			if (FileUtil.exist(CONFIG_FILE)) {
				user = gson.fromJson(new FileReader(CONFIG_FILE), User.class);
			}
		} catch (FileNotFoundException ex) {
			log.info("Config file is missing. Creating new one with default config");
		}
		user.setNick(adminNick);
		return user;
	}
}
