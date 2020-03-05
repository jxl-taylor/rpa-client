package com.mr.rpa.assistant.ui.listtask;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.addtask.TaskAddController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log4j
public class TaskListController implements Initializable {
	@FXML
	private TableView<Task> tableView;
	@FXML
	private TableColumn<Task, String> idCol;
	@FXML
	private TableColumn<Task, String> nameCol;
	@FXML
	private TableColumn<Task, String> cronCol;
	@FXML
	private TableColumn<Task, String> despCol;
	@FXML
	private TableColumn<Task, Boolean> runningCol;
	@FXML
	private TableColumn<Task, Integer> statusCol;
	@FXML
	private TableColumn<Task, Integer> successCountCol;
	@FXML
	private TableColumn<Task, Integer> failCountCol;
	@FXML
	private TableColumn<Task, CheckBox> registeredCol;
	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane contentPane;

	private JFXButton cancelBtn;
	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setTaskTableView(tableView);
		initCol();
		loadData();
		tableView.setItems(taskDao.getTaskList());
		tableView.setRowFactory(tv -> {
			TableRow<Task> row = new TableRow<Task>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty()) {
					if (event.getClickCount() == 2) {
						showEditOption(row.getItem());
					}
					String taskId = row.getItem().getId();
					globalProperty.getSelectedTaskId().set(taskId);
					//load task log
					loadLogData(taskId);
					globalProperty.getSelectedTaskLogId().set("");
				}
			});
			return row;
		});
		cancelBtn = new JFXButton("取消");
		cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			StackPane rootPane = GlobalProperty.getInstance().getRootPane();
			rootPane.getChildren().get(0).setEffect(null);
		});
	}

	private Stage getStage() {
		return (Stage) tableView.getScene().getWindow();
	}

	private void initCol() {
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		cronCol.setCellValueFactory(new PropertyValueFactory<>("cron"));
		despCol.setCellValueFactory(new PropertyValueFactory<>("desp"));
		runningCol.setCellValueFactory(new PropertyValueFactory<>("running"));
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		successCountCol.setCellValueFactory(new PropertyValueFactory<>("successCount"));
		failCountCol.setCellValueFactory(new PropertyValueFactory<>("failCount"));
		registeredCol.setCellValueFactory(new PropertyValueFactory<>("checkBox"));
	}

	private void loadData() {
		taskDao.loadTaskList();
	}

	private void loadLogData(String taskId) {
		taskLogDao.loadTaskLogList(taskId);
	}

	@FXML
	private void handleTaskDeleteOption(ActionEvent event) {
		//Fetch the selected row
		Task selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
		if (selectedForDeletion == null) {
			AlertMaker.showErrorMessage("未选择任务", "请选择一个任务删除.");
			return;
		}
		if (CollectionUtil.isNotEmpty(taskDao.queryTaskByNextTask(selectedForDeletion.getName()))) {
			AlertMaker.showErrorMessage("无法删除", "该任务被别的任务依赖.");
			return;
		}
		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				JobFactory.delete(taskDao.queryTaskById(selectedForDeletion.getId()));
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showSimpleAlert("失败", selectedForDeletion.getName() + " 不能删除");
				return;
			}
			String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath();
			String jobPath = taskFileDir + File.separator + selectedForDeletion.getName();
			FileUtil.del(jobPath);
			boolean result = taskDao.deleteTask(selectedForDeletion);
			taskLogDao.deleteTaskLog(selectedForDeletion.getId());
			if (result) {
				AlertMaker.showSimpleAlert("删除任务", selectedForDeletion.getName() + " 删除成功.");
				taskDao.removeTask(selectedForDeletion);
				taskLogDao.getTaskLogList().clear();
			} else {
				AlertMaker.showSimpleAlert("失败", selectedForDeletion.getName() + " 不能删除");
			}
		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "删除任务",
				"确定删除该任务么 " + selectedForDeletion.getName() + " ?", false);

	}

	@FXML
	private void handleTaskEditOption(ActionEvent event) {
		//Fetch the selected row
		Task selectedForEdit = tableView.getSelectionModel().getSelectedItem();
		showEditOption(selectedForEdit);
	}

	private void showEditOption(Task selectedForEdit) {
		if (selectedForEdit == null) {
			AlertMaker.showErrorMessage("未选择任务", "请选择一个任务编辑.");
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"));
			Parent parent = loader.load();

			TaskAddController controller = (TaskAddController) loader.getController();
			controller.inflateUI(selectedForEdit);

			Stage stage = new Stage(StageStyle.DECORATED);
			stage.setTitle("编辑任务");
			stage.setScene(new Scene(parent));
			stage.show();
			AssistantUtil.setStageIcon(stage);

			stage.setOnHiding((e) -> {
				handleRefresh(new ActionEvent());
			});

		} catch (IOException ex) {
			log.error(ex);
		}
	}

	@FXML
	private void startTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskDao.queryTaskById(selectedTask.getId());
		if (task.isRunning()) {
			AlertMaker.showSimpleAlert("开启任务", "不能重复开启");
			return;
		}

		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				task.setRunning(true);
				JobFactory.add(task);
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showErrorMessage("开启任务", "调度引擎开启任务失败.");
				throw new RuntimeException(e);
			}
			taskDao.updateTaskRunning(task.getId(), true);
			loadData();
			AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "启动任务", selectedTask.getId() + " 已开启");

		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "开启任务", "确定开启该任务么 " + task.getName() + " ?", false);

	}

	@FXML
	private void endTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskDao.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("停止任务", "任务已经停止");
			return;
		}

		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				JobFactory.delete(task);
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showErrorMessage("停止任务", "调度引擎停止任务失败.");
				throw new RuntimeException(e);
			}
			taskDao.updateTaskRunning(selectedTask.getId(), false);
			loadData();
			AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "停止任务", selectedTask.getId() + " 已停止");

		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "停止任务", "确定停止该任务么" + task.getName() + " ?", false);

	}

	@FXML
	private void resumeTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskDao.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("恢复执行", "任务还没启动");
			return;
		}

		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_PAUSE) {
			AlertMaker.showSimpleAlert("恢复执行", "任务未暂停");
			return;
		}

		try {
			task.setStatus(SystemContants.TASK_RUNNING_STATUS_RUN);
			JobFactory.resume(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage("恢复执行", "调度引擎恢复任务失败.");
			return;
		}
		taskDao.updateTaskStatus(task.getId(), SystemContants.TASK_RUNNING_STATUS_RUN);
		loadData();
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "恢复执行", selectedTask.getId() + " 已恢复");
	}

	@FXML
	private void pauseTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskDao.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("暂停执行", "任务还没启动");
			return;
		}

		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_RUN) {
			AlertMaker.showSimpleAlert("暂停执行", "任务已暂停");
			return;
		}

		try {
			task.setStatus(SystemContants.TASK_RUNNING_STATUS_PAUSE);
			JobFactory.pause(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage("暂停执行", "调度引擎暂停任务失败.");
			return;
		}
		taskDao.updateTaskStatus(task.getId(), SystemContants.TASK_RUNNING_STATUS_PAUSE);
		loadData();
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "暂停执行", selectedTask.getId() + " 已暂停 ");
	}

	@FXML
	private void handleRefresh(ActionEvent event) {
		taskLogDao.getTaskLogList().clear();
		loadData();
	}

	public static class Task {

		private final SimpleStringProperty id;
		private final SimpleStringProperty name;
		private final SimpleStringProperty mainTask;
		private final SimpleStringProperty cron;
		private final SimpleStringProperty desp;
		private final SimpleStringProperty params;
		private final SimpleStringProperty nextTask;
		private final SimpleStringProperty running;
		private final SimpleStringProperty status;
		private final SimpleIntegerProperty successCount;
		private final SimpleIntegerProperty failCount;

		private CheckBox checkBox = new CheckBox();

		public Task(String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running, Integer status, String cron, Integer successCount, Integer failCount) {
			this.id = new SimpleStringProperty(id);
			this.name = new SimpleStringProperty(name);
			this.mainTask = new SimpleStringProperty(mainTask);
			this.cron = new SimpleStringProperty(cron);
			this.desp = new SimpleStringProperty(desp);
			this.params = new SimpleStringProperty(params);
			this.nextTask = new SimpleStringProperty(nextTask);
			if (running) {
				this.running = new SimpleStringProperty("已开启");
			} else {
				this.running = new SimpleStringProperty("未开启");
			}
			if (status == 0) {
				this.status = new SimpleStringProperty("暂停");
			} else {
				this.status = new SimpleStringProperty("正常");
			}
			this.successCount = new SimpleIntegerProperty(successCount);
			this.failCount = new SimpleIntegerProperty(failCount);
		}

		public String getId() {
			return id.get();
		}

		public String getName() {
			return name.get();
		}

		public String getMainTask() {
			return mainTask.get();
		}

		public String getCron() {
			return cron.get();
		}

		public String getDesp() {
			return desp.get();
		}

		public String getParams() {
			return params.get();
		}

		public String getNextTask() {
			return nextTask.get();
		}

		public String getRunning() {
			return running.get();
		}

		public String getStatus() {
			return status.get();
		}

		public int getSuccessCount() {
			return successCount.get();
		}

		public int getFailCount() {
			return failCount.get();
		}

	}

}
