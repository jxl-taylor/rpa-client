package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.io.FileUtil;
import com.google.gson.Gson;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mr.rpa.assistant.alert.AlertMaker;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.digest.DigestUtils;

@Log4j
public class Preferences {

	public static final String CONFIG_FILE = System.getProperty("user.dir") + File.separator + "config.txt";
	@Getter
	String username;
	@Getter
	String password;

	public Preferences() {
		//设置默认用户名密码
		username = "admin";
		setPassword("admin");
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static Preferences getPreferences() {
		Gson gson = new Gson();
		Preferences preferences = new Preferences();
		try {
			if (FileUtil.exist(CONFIG_FILE)) {
				preferences = gson.fromJson(new FileReader(CONFIG_FILE), Preferences.class);
			}

		} catch (FileNotFoundException ex) {
			log.error("Config file is missing. Creating new one with default config");
			throw new RuntimeException(ex);
		}
		return preferences;
	}

}
