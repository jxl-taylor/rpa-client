package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class TaskLogController implements Initializable {

	@FXML
	private JFXButton logButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		logButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			boolean visible = globalProperty.getTaskLogPaneVisible().get();
			globalProperty.getTaskLogPaneVisible().set(!visible);
			globalProperty.getTaskHistoryPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(visible);
		});
	}
}
