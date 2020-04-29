package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXTextArea;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.LogTextCollector;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class SmallTaskLogController implements Initializable, ILogShow {

	@FXML
	private JFXTextArea logTextArea;

	@FXML
	private MenuItem logMenu;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setLogTextArea(logTextArea);
		globalProperty.getLogShows().add(this);
		logTextArea.textProperty().bind(globalProperty.getAllLog());
	}

	private void maximizeLog(){
		globalProperty.getTaskLogPaneVisible().set(true);
		globalProperty.getTaskHistoryPaneVisible().set(false);
		globalProperty.getTaskPaneVisible().set(false);
		globalProperty.getLogListHeight().set(globalProperty.MAX_LOG_LIST_HEIGHT);
		globalProperty.getTaskBeanController().refreshSplit();
	}

	@FXML
	private void clearLog(ActionEvent event) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.getLogTextCollector().clearAllLog();
	}

	@FXML
	private void stopLog(ActionEvent event) {
		LogTextCollector logTextCollector = globalProperty.getLogTextCollector();
		if (logTextCollector.isLogAble()) {
			logTextCollector.setLogAble(false);
			logMenu.setText("开启日志");
		} else {
			logTextCollector.setLogAble(true);
			logMenu.setText("停止日志");
		}
	}

	public void scrollText() {
		Platform.runLater(() -> {
			logTextArea.selectEnd();
			logTextArea.deselect();
		});
	}

	@Override
	public void setMenuName(String name) {
		logMenu.setText(name);
	}

	@Override
	public void refreshLog() {
		LogTextCollector logTextCollector = globalProperty.getLogTextCollector();
		logTextCollector.setLogAble(false);
		logMenu.setText("开启日志");
	}

}
