package com.mr.rpa.assistant.ui.addtask;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

@Log4j
public class TaskAddController implements Initializable {

	@FXML
	private JFXTextField id;
	@FXML
	private JFXTextField name;
	@FXML
	private JFXTextField cron;
	@FXML
	private JFXTextArea desp;
	@FXML
	private JFXButton saveButton;
	@FXML
	private JFXButton cancelButton;
	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;

	private SimpleBooleanProperty isInEditMode = new SimpleBooleanProperty();

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		id.visibleProperty().bind(isInEditMode);
	}

	@FXML
	private void addTask(ActionEvent event) {
		String taskName = StringUtils.trimToEmpty(name.getText());
		String taskCron = StringUtils.trimToEmpty(cron.getText());
		String taskDesp = StringUtils.trimToEmpty(desp.getText());

		if (taskName.isEmpty() || taskDesp.isEmpty() || taskCron.isEmpty()) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "请正确输入.");
			return;
		}

		if (isInEditMode.getValue()) {
			handleEditOperation();
			return;
		}
		id.setText(UUID.randomUUID().toString().replace("-", ""));
		Task task = new Task(id.getText(), name.getText(), desp.getText(),
				true, SystemContants.TASK_RUNNING_STATUS_RUN, cron.getText(),
				0, 0);
		try {
			JobFactory.add(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增任务", "调度添加失败");
			return;
		}
		boolean result = taskDao.insertNewTask(task);
		if (result) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增任务", taskName + " 已添加");
			clearEntries();
			taskDao.loadTaskList();
		} else {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增失败", "请检查输入");
		}
	}

	@FXML
	private void cancel(ActionEvent event) {
		Stage stage = (Stage) rootPane.getScene().getWindow();
		stage.close();
	}

	public void inflateUI(TaskListController.Task task) {
		id.setText(task.getId());
		name.setText(task.getName());
		cron.setText(task.getCron());
		desp.setText(task.getDesp());
		id.setEditable(false);
		isInEditMode.setValue(Boolean.TRUE);
	}

	private void clearEntries() {
		id.clear();
		name.clear();
		cron.clear();
		desp.clear();
	}

	private void handleEditOperation() {
		TaskListController.Task task = new TaskListController.Task(id.getText(), name.getText(), desp.getText(),
				false, 0, cron.getText(),0, 0);
		try {
			Task taskModel = taskDao.queryTaskById(task.getId());
			taskModel.setCron(task.getCron());
			taskModel.setDesp(task.getDesp());
			JobFactory.update(taskModel);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
		}
		if (taskDao.updateTask(task)) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Success", "修改成功");
		} else {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
		}
	}
}
