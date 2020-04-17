package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXButton;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class LargeTaskLogController implements Initializable, ILogShow {

	@FXML
	private VBox rootPane;
	@FXML
	private Label logLabel;

	@FXML
	private JFXTextArea logTextArea;

	@FXML
	private MenuItem logMenu;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		logTextArea.setMinHeight(globalProperty.MAX_LOG_HEIGHT);
		logTextArea.setMaxHeight(globalProperty.MAX_LOG_HEIGHT);
		logLabel.setOnMouseClicked((Event event) -> {
			globalProperty.getTaskLogPaneVisible().set(false);
			globalProperty.getTaskHistoryPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(true);
			globalProperty.getTaskBeanController().refreshSplit();
		});

		globalProperty.getLogShows().add(this);
		logTextArea.textProperty().bind(globalProperty.getAllLog());

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
		logTextCollector.setLogAble(true);
		logMenu.setText("停止日志");
	}
}
