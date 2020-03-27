package com.mr.rpa.assistant.ui.addtask;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.extern.log4j.Log4j;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j
public class TaskLogDetailController implements Initializable {
	@FXML
	private JFXTextField id;
	@FXML
	private JFXTextField taskLogId;
	@FXML
	private JFXTextField taskId;
	@FXML
	private JFXTextField taskName;
	@FXML
	private JFXTextField status;
	@FXML
	private JFXTextArea error;
	@FXML
	private JFXTextField startTime;
	@FXML
	private JFXTextField endTime;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);


	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	public void load(TaskLogListController.TaskLog taskLog) {
		id.setText(String.valueOf(taskLog.getId()));
		taskLogId.setText(taskLog.getTaskLogId());
		taskId.setText(taskLog.getTaskId());
		Task task =taskService.queryTaskById(taskLog.getTaskId());
		taskName.setText(task.getName());
		status.setText(taskLog.getStatus());
		error.setText(taskLog.getError());
		startTime.setText(taskLog.getStartTime());
		endTime.setText(taskLog.getEndTime());
	}

}
