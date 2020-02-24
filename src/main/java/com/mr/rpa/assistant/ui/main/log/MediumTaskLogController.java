package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class MediumTaskLogController implements Initializable, ILogShow {

	@FXML
	private VBox rootPane;
	@FXML
	private JFXButton logButton;

	@FXML
	private JFXTextArea logTextArea;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		logTextArea.setMinHeight(globalProperty.MAX_LOG_HEIGHT);
		logTextArea.setMaxHeight(globalProperty.MAX_LOG_HEIGHT);
		logButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskLogPaneVisible().set(true);
			globalProperty.getTaskHistoryPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(false);
			globalProperty.getLogListHeight().set(globalProperty.MAX_LOG_LIST_HEIGHT);
			globalProperty.getTaskBeanController().refreshSplit();
		});

		globalProperty.getLogShows().add(this);
		logTextArea.textProperty().bind(globalProperty.getSelectedLog());
	}

	public void appendText(String text) {
		logTextArea.appendText(text);
	}

	public String getLogText() {
		return logTextArea.getText();
	}

	public void scrollText() {
		Platform.runLater(() -> {
			logTextArea.selectEnd();
			logTextArea.deselect();
		});
	}
}
