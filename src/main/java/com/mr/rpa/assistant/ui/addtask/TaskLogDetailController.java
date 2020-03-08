package com.mr.rpa.assistant.ui.addtask;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j
@Component
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

	@Autowired
	private TaskDao taskDao;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	public void load(TaskLogListController.TaskLog taskLog) {
		id.setText(taskLog.getId());
		taskLogId.setText(taskLog.getTaskLogId());
		taskId.setText(taskLog.getTaskId());
		Task task =taskDao.queryTaskById(taskLog.getTaskId());
		taskName.setText(task.getName());
		status.setText(taskLog.getStatus());
		error.setText(taskLog.getError());
		startTime.setText(taskLog.getStartTime());
		endTime.setText(taskLog.getEndTime());
	}

}
