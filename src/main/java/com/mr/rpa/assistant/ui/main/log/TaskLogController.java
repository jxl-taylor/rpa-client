package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
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
public class TaskLogController implements Initializable {

	@FXML
	private VBox rootPane;
	@FXML
	private JFXButton logButton;

	@FXML
	private JFXTextArea logTextArea;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		logButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			boolean visible = globalProperty.getTaskLogPaneVisible().get();
			globalProperty.getTaskLogPaneVisible().set(!visible);
			globalProperty.getTaskHistoryPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(visible);
			globalProperty.getLogAreaMinHeight().set(visible ? globalProperty.DEFAULT_LOG_HEIGHT : globalProperty.MAX_LOG_HEIGHT);
			globalProperty.getLogListHeight().set(globalProperty.DEFAULT_LOG_LIST_HEIGHT);
			globalProperty.getMainController().refreshSplit();
		});

		logTextArea.textProperty().bind(globalProperty.getSelectedLog());
		logTextArea.minHeightProperty().bind(globalProperty.getLogAreaMinHeight());
		logTextArea.maxHeightProperty().bind(globalProperty.getLogAreaMinHeight());
		globalProperty.getLogAreaMinHeight().set(globalProperty.DEFAULT_LOG_HEIGHT);
	}
}
