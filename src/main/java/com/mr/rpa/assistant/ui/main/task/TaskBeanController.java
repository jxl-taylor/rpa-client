package com.mr.rpa.assistant.ui.main.task;

import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.Pair;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;

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
	private JFXTextField taskID;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setTaskBeanController(this);

		// Listen to the position property
		taskSplit.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
			globalProperty.getTaskTableView().setMaxHeight(taskSplit.getHeight() * newVal.doubleValue() - 80);
			globalProperty.getLogTextArea().setMaxHeight(taskSplit.getHeight() * (1 - newVal.doubleValue()) - 30);
			globalProperty.getLogTextArea().setMinHeight(taskSplit.getHeight() * (1 - newVal.doubleValue()) - 30);
			double logListHeight = taskSplit.getHeight() * (1 - newVal.doubleValue()) - 80;
			globalProperty.getLogListHeight().set(logListHeight > 0 ? logListHeight : 0);
		});

	}

	@FXML
	private void loadTaskInfo(ActionEvent event) {
		String taskId = taskID.getText();
		taskDao.loadTaskList(taskId, null);
		taskLogDao.getTaskLogList().clear();
		taskSplit.setDividerPositions(SPLIT_POSITION_TASK_AND_LOG);
	}

	@FXML
	private void loadAddTask(ActionEvent event) {
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader()
				.getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);
		pair.getObject1().setOnCloseRequest((e) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/addtask/cron_setting.fxml"));
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

}
