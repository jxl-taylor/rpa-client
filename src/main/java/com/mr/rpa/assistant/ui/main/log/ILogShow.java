package com.mr.rpa.assistant.ui.main.log;

/**
 * Created by feng on 2020/2/21
 */
public interface ILogShow {

	void appendText(String text);

	String getLogText();

	void scrollText();
}
