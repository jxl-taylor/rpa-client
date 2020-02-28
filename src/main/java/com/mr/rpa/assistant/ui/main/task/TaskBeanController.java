package com.mr.rpa.assistant.ui.main.task;

import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/21
 */
public class TaskBeanController implements Initializable {

	@FXML
	private SplitPane taskSplit;

	@FXML
	private JFXTextField taskID;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty.getInstance().setTaskBeanController(this);
	}

	@FXML
	private void loadTaskInfo(ActionEvent event) {
		String taskId = taskID.getText();
		taskDao.loadTaskList(taskId, null);
		taskLogDao.getTaskLogList().clear();
	}

	@FXML
	private void loadAddTask(ActionEvent event) {
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);

	}

	public void refreshSplit() {
		this.taskSplit.setDividerPositions(0.6);
	}
}
