package com.mr.rpa.assistant.ui.addtask;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class TaskAddController implements Initializable {

	@FXML
	private JFXTextField id;
	@FXML
	private JFXTextField name;
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

	private DatabaseHandler databaseHandler;
	private SimpleBooleanProperty isInEditMode = new SimpleBooleanProperty();

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		databaseHandler = DatabaseHandler.getInstance();
		id.visibleProperty().bind(isInEditMode);
	}

	@FXML
	private void addTask(ActionEvent event) {
		String taskName = StringUtils.trimToEmpty(name.getText());
		String taskDesp = StringUtils.trimToEmpty(desp.getText());

		if (taskName.isEmpty() || taskDesp.isEmpty()) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "请正确输入.");
			return;
		}

		if (isInEditMode.getValue()) {
			handleEditOperation();
			return;
		}
		id.setText(UUID.randomUUID().toString().replace("-", ""));

		Task task = new Task(id.getText(), name.getText(), desp.getText(), Boolean.FALSE, 0, 0, 0);
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
		desp.setText(task.getDesp());
		id.setEditable(false);
		isInEditMode.setValue(Boolean.TRUE);
	}

	private void clearEntries() {
		id.clear();
		name.clear();
		desp.clear();
	}

	private void handleEditOperation() {
		TaskListController.Task task = new TaskListController.Task(id.getText(), name.getText(), desp.getText(), false, 0, 0, 0);
		if (taskDao.updateTask(task)) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Success", "修改成功");
		} else {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
		}
	}
}
