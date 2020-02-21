package com.mr.rpa.assistant.ui.main.log;

import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class TaskHistoryController implements Initializable {

	@FXML
	private JFXButton historyButton;

	@FXML
	private ChoiceBox<Status> logStatusChoice;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logStatusChoice.setItems(FXCollections.observableArrayList(
				new Status(-1, "所有状态"),
				new Status(0, "正在执行"),
				new Status(1, "执行成功"),
				new Status(2, "执行失败")
		));
		logStatusChoice.setValue(logStatusChoice.getItems().get(0));
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		historyButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			boolean visible = globalProperty.getTaskHistoryPaneVisible().get();
			globalProperty.getTaskHistoryPaneVisible().set(!visible);
			globalProperty.getTaskLogPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(visible);
			globalProperty.getLogListHeight().set(
					visible ? globalProperty.DEFAULT_LOG_LIST_HEIGHT : globalProperty.MAX_LOG_LIST_HEIGHT);
			globalProperty.getLogAreaMinHeight().set(
					visible ? globalProperty.DEFAULT_LOG_HEIGHT : globalProperty.MAX_LOG_HEIGHT);
			globalProperty.getTaskBeanController().refreshSplit();
		});
	}

	@AllArgsConstructor
	class Status {
		private Integer key;
		private String value;

		@Override
		public String toString(){
			return value;
		}
	}
}
