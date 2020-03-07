package com.mr.rpa.assistant.ui.main.log;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/9 0009
 */
public class TaskHistoryController implements Initializable {

	@FXML
	private Label historyLabel;

	@FXML
	private ChoiceBox<Status> logStatusChoice;

	@FXML
	private ChoiceBox<MaxRow> maxRowChoice;

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	private static int currentStatus;

	private static int currentMaxRow;

	private static List<TaskHistoryController> controllers = Lists.newArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logStatusChoice.setItems(FXCollections.observableArrayList(
				new Status(-1, "所有状态"),
				new Status(0, "正在执行"),
				new Status(1, "执行成功"),
				new Status(2, "执行失败")
		));
		logStatusChoice.setValue(logStatusChoice.getItems().get(0));
		logStatusChoice.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> {
					currentStatus = newValue.key;
					setLogStatusChoice();
					loadTaskLog(null);
				});

		maxRowChoice.setItems(FXCollections.observableArrayList(
				new MaxRow(100, "最多显示100条"),
				new MaxRow(300, "最多显示300条"),
				new MaxRow(500, "最多显示500条"),
				new MaxRow(1000, "最多显示1000条"),
				new MaxRow(1500, "最多显示1500条"),
				new MaxRow(2000, "最多显示2000条")
		));

		maxRowChoice.setValue(maxRowChoice.getItems().get(0));
		maxRowChoice.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> {
					taskLogDao.setMaxRow(newValue.key);
					currentMaxRow = newValue.key;
					setMaxRowChoice();
					loadTaskLog(null);
				});
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		historyLabel.setOnMouseClicked((Event event) -> {
			boolean visible = globalProperty.getTaskHistoryPaneVisible().get();
			globalProperty.getTaskHistoryPaneVisible().set(!visible);
			globalProperty.getTaskLogPaneVisible().set(false);
			globalProperty.getTaskPaneVisible().set(visible);
			if (!visible) {
				globalProperty.getLogListHeight().set(globalProperty.MAX_LOG_LIST_HEIGHT);
				globalProperty.getLogAreaMinHeight().set(globalProperty.MAX_LOG_HEIGHT);
			} else {
				globalProperty.getTaskBeanController().refreshSplit();
			}
		});
		controllers.add(this);
	}

	private void setLogStatusChoice() {
		controllers.forEach(controller -> {
			controller.logStatusChoice.getItems().forEach(item -> {
				if (item.key == currentStatus) controller.logStatusChoice.setValue(item);
			});
		});


	}

	private void setMaxRowChoice() {
		controllers.forEach(controller -> {
			controller.maxRowChoice.getItems().forEach(item -> {
				if (item.key == currentMaxRow) controller.maxRowChoice.setValue(item);
			});
		});
	}

	@FXML
	public void loadTaskLog(ActionEvent actionEvent) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		int status = logStatusChoice.getValue().key;
		if (status == -1) {
			taskLogDao.loadTaskLogList(globalProperty.getSelectedTaskId().get());
		} else {
			taskLogDao.loadTaskLogList(globalProperty.getSelectedTaskId().get(), logStatusChoice.getValue().key);
		}

	}

	@AllArgsConstructor
	static
	class Status {
		private Integer key;
		private String value;

		@Override
		public String toString() {
			return value;
		}
	}

	@AllArgsConstructor
	static
	class MaxRow {
		private Integer key;
		private String value;

		@Override
		public String toString() {
			return value;
		}
	}
}
