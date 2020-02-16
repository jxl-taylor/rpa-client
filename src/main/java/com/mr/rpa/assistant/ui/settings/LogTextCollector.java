package com.mr.rpa.assistant.ui.settings;

/**
 * Created by feng on 2020/2/16 0016
 */
public class LogTextCollector {

	private StringBuilder allSb = new StringBuilder();

	public void addLog(String logText){
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		allSb.append(logText).append("\n");
		globalProperty.getSelectedLog().set(getAllLog());
	}

	public String getAllLog(){
		return allSb.toString();
	}

}
