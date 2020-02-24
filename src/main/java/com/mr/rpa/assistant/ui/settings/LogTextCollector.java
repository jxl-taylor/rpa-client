package com.mr.rpa.assistant.ui.settings;

import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by feng on 2020/2/16 0016
 */
public class LogTextCollector {

	private StringBuilder allSb = new StringBuilder();

	private int allLogRowLimit = 0;

	private StringBuilder selectSb = new StringBuilder();

	private int selectLogRowLimit = 0;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	public void addLog(String logText) {
		Platform.runLater(() -> {
			if(globalProperty == null) globalProperty = GlobalProperty.getInstance();
			//超过限制，删除第一条
			if (allLogRowLimit > 200) {
				allSb.delete(0, allSb.indexOf("\n") + 1);
				System.out.println("allSb.indexOf = " + allSb.indexOf("\n"));
			}
			allSb.append(logText).append("\n");
			allLogRowLimit++;
			if (StringUtils.isNotEmpty(globalProperty.getSelectedTaskLogId().get())) {
				if (selectLogRowLimit > 200) {
					selectSb.delete(0, allSb.indexOf("\n") + 1);
				}
				selectSb.append(logText).append("\n");
				selectLogRowLimit++;
			}
			globalProperty.getSelectedLog().set(getAllLog());
		});
	}

	public String getAllLog() {
		return StringUtils.isEmpty(globalProperty.getSelectedTaskLogId().get()) ? allSb.toString() : selectSb.toString();
	}

	public void clearSelectLog() {
		selectSb = new StringBuilder();
		allLogRowLimit = 0;
	}

	public void clearAllLog() {
		allSb = new StringBuilder();
		selectLogRowLimit = 0;
	}

}
