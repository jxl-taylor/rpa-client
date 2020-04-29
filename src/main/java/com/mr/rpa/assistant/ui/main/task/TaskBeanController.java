package com.mr.rpa.assistant.ui.main.task;

import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.Pair;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import java.net.URL;
import java.util.ResourceBundle;

import static com.mr.rpa.assistant.ui.settings.GlobalProperty.SPLIT_POSITION_TASK_AND_LOG;

/**
 * Created by feng on 2020/2/21
 */
@Log4j
public class TaskBeanController implements Initializable {

	@FXML
	private SplitPane taskSplit;

	@FXML
	private BorderPane taskListPane;

	@FXML
	private JFXTextField taskName;

	@FXML
	private VBox historyContainer;

	@FXML
	private AnchorPane historyPane;

	@FXML
	private AnchorPane logPane;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		globalProperty.setTaskBeanController(this);
		historyContainer.getChildren().remove(logPane);
		historyContainer.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				maximizeHistory();
			}
		});
		// Listen to the position property
		taskSplit.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
			globalProperty.getTaskTableView().setMaxHeight(taskSplit.getHeight() * newVal.doubleValue() - 80);
			if (globalProperty.getLogTextArea() != null) {
				globalProperty.getLogTextArea().setMaxHeight(taskSplit.getHeight() * (1 - newVal.doubleValue()) - 80);
				globalProperty.getLogTextArea().setMinHeight(taskSplit.getHeight() * (1 - newVal.doubleValue()) - 80);
				double logListHeight = taskSplit.getHeight() * (1 - newVal.doubleValue()) - 130;
				globalProperty.getLogListHeight().set(logListHeight > 0 ? logListHeight : 0);
			}
		});

	}

	public void maximizeHistory() {
		if (globalProperty.getLogListHeight().get() < globalProperty.MAX_LOG_LIST_HEIGHT) {
			taskSplit.getItems().remove(0);
			globalProperty.getLogListHeight().set(globalProperty.MAX_LOG_LIST_HEIGHT);
			globalProperty.getLogTextArea().setMinHeight(globalProperty.MAX_LOG_HEIGHT);
			globalProperty.getLogTextArea().setMaxHeight(globalProperty.MAX_LOG_HEIGHT);
		} else {
			taskSplit.getItems().add(0, taskListPane);
			refreshSplit();
		}
	}

	@FXML
	private void loadTaskInfo(ActionEvent event) {
		taskService.loadUITaskList(null, taskName.getText());
		taskLogService.getUITaskLogList().clear();
		taskSplit.setDividerPositions(SPLIT_POSITION_TASK_AND_LOG);
	}

	@FXML
	public void clearSearch(ActionEvent event) {
		taskName.setText("");
		loadTaskInfo(null);
	}

	@FXML
	private void loadAddTask(ActionEvent event) {
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader()
				.getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);
		pair.getObject1().setOnCloseRequest((e) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"));
			Pair<Stage, Object> cronPair = AssistantUtil.getWindow(getClass()
					.getClassLoader().getResource("assistant/ui/addtask/cron_setting.fxml"));
			if (cronPair != null) cronPair.getObject1().close();
		});

	}

	public void refreshSplit() {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.getTaskTableView().setMaxHeight(taskSplit.getHeight() * taskSplit.getDividerPositions()[0] - 80);
		globalProperty.getLogTextArea().setMaxHeight(taskSplit.getHeight() * (1 - taskSplit.getDividerPositions()[0]) - 30);
		globalProperty.getLogTextArea().setMinHeight(taskSplit.getHeight() * (1 - taskSplit.getDividerPositions()[0]) - 30);
		double logListHeight = taskSplit.getHeight() * (1 - taskSplit.getDividerPositions()[0]) - 80;
		globalProperty.getLogListHeight().set(logListHeight > 0 ? logListHeight : 0);
	}

	public void refreshSplit(double position) {
		this.taskSplit.setDividerPositions(position);
	}

	@FXML
	public void showHistory(ActionEvent event) {
		ObservableList<Node> observableList = historyContainer.getChildren();
		if (!observableList.contains(historyPane)) {
			observableList.add(historyPane);
			observableList.remove(logPane);
		}
	}

	@FXML
	public void showLog(ActionEvent event) {
		ObservableList<Node> observableList = historyContainer.getChildren();
		if (!observableList.contains(logPane)) {
			observableList.add(logPane);
			observableList.remove(historyPane);
		}
	}
}
