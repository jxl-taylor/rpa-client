package com.mr.rpa.assistant.util;

import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

/**
 * Created by feng on 2020/3/8 0008
 */
public class CommonUtil {

	public static <T> T loadFXml(URL location) throws IOException {
		return getFxmlLoader(location).load();
	}

	public static FXMLLoader getFxmlLoader(URL location) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(location);
		fxmlLoader.setControllerFactory(GlobalProperty.applicationContext::getBean);
		return fxmlLoader;
	}
}
