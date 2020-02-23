package com.mr.rpa.assistant.ui.listtask;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.addtask.TaskAddController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
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
	private StackPane rootPane;

	@FXML
	private AnchorPane contentPane;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initCol();
		loadData();
		tableView.setItems(taskDao.getTaskList());
		tableView.setRowFactory(tv -> {
			TableRow<Task> row = new TableRow<Task>();
			row.setOnMouseClicked(event -> {
				if(!row.isEmpty()){
					if (event.getClickCount() == 2) {
						showEditOption(row.getItem());
					}
					String taskId = row.getItem().getId();
					GlobalProperty.getInstance().getSelectedTaskId().set(taskId);
					//load task log
					loadLogData(taskId);
				}
			});
			return row;
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
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("删除任务");
		alert.setContentText("确定删除该任务么 " + selectedForDeletion.getName() + " ?");
		Optional<ButtonType> answer = alert.showAndWait();
		if (answer.get() == ButtonType.OK) {
			try {
				JobFactory.delete(taskDao.queryTaskById(selectedForDeletion.getId()));
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showSimpleAlert("失败", selectedForDeletion.getName() + " 不能删除");
				return;
			}
			Boolean result = taskDao.deleteTask(selectedForDeletion);
			taskLogDao.deleteTaskLog(selectedForDeletion.getId());
			if (result) {
				AlertMaker.showSimpleAlert("删除任务", selectedForDeletion.getName() + " 删除成功.");
				taskDao.removeTask(selectedForDeletion);
			} else {
				AlertMaker.showSimpleAlert("失败", selectedForDeletion.getName() + " 不能删除");
			}
		} else {
			AlertMaker.showSimpleAlert("取消删除", "删除已取消");
		}
	}

	@FXML
	private void handleTaskEditOption(ActionEvent event) {
		//Fetch the selected row
		Task selectedForEdit = tableView.getSelectionModel().getSelectedItem();
		showEditOption(selectedForEdit);
	}

	private void showEditOption(Task selectedForEdit){

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
			LibraryAssistantUtil.setStageIcon(stage);

			stage.setOnHiding((e) -> {
				handleRefresh(new ActionEvent());
			});

		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void startTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		com.mr.rpa.assistant.data.model.Task task =  taskDao.queryTaskById(selectedTask.getId());
		if(task.isRunning()){
			AlertMaker.showSimpleAlert("开启任务", "不能重复开启");
			return;
		}
		try {
			JobFactory.delete(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage("开启任务", "调度器开启任务失败.");
		}
		taskDao.updateTaskRunning(task.getId(), true);
		loadData();
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "启动任务", selectedTask.getId() + " 已开启");
}

	@FXML
	private void endTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		com.mr.rpa.assistant.data.model.Task task =  taskDao.queryTaskById(selectedTask.getId());
		if(task.isRunning()){
			AlertMaker.showSimpleAlert("停止任务", "任务已经停止");
			return;
		}
		try {
			JobFactory.delete(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage("停止任务", "调度器停止任务失败.");
		}
		taskDao.updateTaskRunning(selectedTask.getId(), false);
		loadData();
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "停止任务", selectedTask.getId() + " 已停止");
	}

	@FXML
	private void handleRefresh(ActionEvent event) {
		loadData();
	}

	public static class Task {

		private final SimpleStringProperty id;
		private final SimpleStringProperty name;
		private final SimpleStringProperty cron;
		private final SimpleStringProperty desp;
		private final SimpleStringProperty running;
		private final SimpleStringProperty status;
		private final SimpleIntegerProperty successCount;
		private final SimpleIntegerProperty failCount;

		public Task(String id, String name, String desp, Boolean running, Integer status, String cron, Integer successCount, Integer failCount) {
			this.id = new SimpleStringProperty(id);
			this.name = new SimpleStringProperty(name);
			this.cron = new SimpleStringProperty(cron);
			this.desp = new SimpleStringProperty(desp);
			if (running) {
				this.running = new SimpleStringProperty("已开启");
			} else {
				this.running = new SimpleStringProperty("未开启");
			}
			if (status == 0) {
				this.status = new SimpleStringProperty("暂停中");
			} else {
				this.status = new SimpleStringProperty("运行中");
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

		public String getCron() {
			return cron.get();
		}

		public String getDesp() {
			return desp.get();
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
