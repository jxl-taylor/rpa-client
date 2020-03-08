package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import javafx.application.Platform;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by feng on 2020/2/16 0016
 */
@Component
public class LogTextCollector {

	private StringBuilder allSb = new StringBuilder();

	private int allLogRowLimit = 0;

	private boolean logAble = true;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	public void addLog(String logText) {
		Platform.runLater(() -> {
			if(globalProperty == null) globalProperty = GlobalProperty.getInstance();
			if(!logAble) return;
			//超过限制，删除第一条
			if (allLogRowLimit > 200) {
				allSb.delete(0, allSb.indexOf("\n") + 1);
			}
			allSb.append(String.format("[%s] %s\n", DateUtil.format(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN), logText));
			allLogRowLimit++;
			globalProperty.getAllLog().set(getAllLog());
		});
	}

	public String getAllLog() {
		return allSb.toString();
	}

	public void clearAllLog() {
		allSb = new StringBuilder();
		allLogRowLimit = 0;
		globalProperty.getAllLog().set(getAllLog());
	}

	public boolean isLogAble() {
		return logAble;
	}

	public void setLogAble(boolean logAble) {
		this.logAble = logAble;
	}
}
