package com.mr.rpa.assistant.ui.main.log;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.addtask.TaskAddController;
import com.mr.rpa.assistant.ui.addtask.TaskLogDetailController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

@Log4j
public class TaskLogListController implements Initializable {

	@FXML
	private TableView<TaskLog> tableView;
	@FXML
	private TableColumn<TaskLog, Integer> idCol;
	@FXML
	private TableColumn<TaskLog, String> statusCol;
	@FXML
	private TableColumn<TaskLog, String> errorCol;
	@FXML
	private TableColumn<TaskLog, String> startTime;
	@FXML
	private TableColumn<TaskLog, String> endTime;
	@FXML
	private AnchorPane rootPane;

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		initCol();
		tableView.setItems(taskLogDao.getTaskLogList());
		tableView.setRowFactory(tv -> {
			TableRow<TaskLog> row = new TableRow<TaskLog>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty()) {
					if (event.getClickCount() == 2) {
						showLogDetail(row.getItem());
					}
					String taskLogId = row.getItem().getId();
					globalProperty.getSelectedTaskLogId().set(taskLogId);
				}
			});
			return row;
		});

		tableView.minHeightProperty().bind(globalProperty.getLogListHeight());
		tableView.maxHeightProperty().bind(globalProperty.getLogListHeight());
		globalProperty.getLogListHeight().set(globalProperty.DEFAULT_LOG_LIST_HEIGHT);
	}

	private Stage getStage() {
		return (Stage) tableView.getScene().getWindow();
	}

	private void initCol() {
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		errorCol.setCellValueFactory(new PropertyValueFactory<>("error"));
		startTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		endTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
	}

	private List<TaskLog> loadLogData() {
		return taskLogDao.loadTaskLogList(GlobalProperty.getInstance().getSelectedTaskId().get());
	}

	@FXML
	private void reRun(ActionEvent event) {
		//Fetch the selected row
		TaskLog selectedForDetail = tableView.getSelectionModel().getSelectedItem();
		if (selectedForDetail == null) {
			AlertMaker.showErrorMessage("未选择任务日志", "请选择一条记录.");
			return;
		}

		try {
			com.mr.rpa.assistant.data.model.TaskLog taskLog = taskLogDao.loadTaskLogById(selectedForDetail.getTaskLogId());
			if (taskLog.getStatus() == SystemContants.TASK_LOG_STATUS_RUNNING) {
				AlertMaker.showErrorMessage("重新执行", "任务运行中，请稍等再试.");
				return;
			}
			JobFactory.trigger(taskLog);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage("开启任务", "调度引擎开启任务失败.");
			return;
		}

	}

	@FXML
	private void handleTaskDetailOption(ActionEvent event) {
		//Fetch the selected row
		TaskLog selectedForDetail = tableView.getSelectionModel().getSelectedItem();
		showLogDetail(selectedForDetail);

	}

	private void showLogDetail(TaskLogListController.TaskLog selectedForDetail) {

		if (selectedForDetail == null) {
			AlertMaker.showErrorMessage("未选择任务日志", "请选择一条记录.");
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("assistant/ui/addtask/task_log.fxml"));
			Parent parent = loader.load();

			TaskLogDetailController controller = (TaskLogDetailController) loader.getController();
			controller.load(selectedForDetail);
			Stage stage = new Stage(StageStyle.DECORATED);
			stage.setTitle("任务日志详情");
			stage.setScene(new Scene(parent));
			stage.show();
			LibraryAssistantUtil.setStageIcon(stage);

			stage.setOnHiding((e) -> {
				handleRefresh(new ActionEvent());
			});

		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void handleRefresh(ActionEvent event) {
		loadLogData();
	}

	@FXML
	private void closeStage(ActionEvent event) {
		getStage().close();
	}

	public static class TaskLog {

		private final SimpleStringProperty id;
		private final SimpleStringProperty taskLogId;
		private final SimpleStringProperty taskId;
		private final SimpleStringProperty status;
		private final SimpleStringProperty error;
		private final SimpleStringProperty startTime;
		private final SimpleStringProperty endTime;

		public TaskLog(String id, String taskLogId, String taskId, Integer status, String error, java.util.Date startTime, java.util.Date endTime) {
			this.id = new SimpleStringProperty(id);
			this.taskLogId = new SimpleStringProperty(taskLogId);
			this.taskId = new SimpleStringProperty(taskId);
			if (status == SystemContants.TASK_LOG_STATUS_RUNNING) {
				this.status = new SimpleStringProperty("正在执行");
			} else if (status == SystemContants.TASK_LOG_STATUS_SUCCESS) {
				this.status = new SimpleStringProperty("执行成功");
			} else if (status == SystemContants.TASK_LOG_STATUS_FAIL) {
				this.status = new SimpleStringProperty("执行失败");
			} else {
				this.status = new SimpleStringProperty("未知状态");
			}
			this.error = new SimpleStringProperty(error);
			this.startTime = new SimpleStringProperty(DateFormatUtils.format(startTime, "yyyy-MM-dd HH:mm:ss"));
			if (endTime != null) {
				this.endTime = new SimpleStringProperty(DateFormatUtils.format(endTime, "yyyy-MM-dd HH:mm:ss"));
			} else {
				this.endTime = new SimpleStringProperty();
			}
		}

		public String getId() {
			return id.get();
		}

		public String getTaskLogId() {
			return taskLogId.get();
		}

		public String getTaskId() {
			return taskId.get();
		}

		public String getStatus() {
			return status.get();
		}

		public String getError() {
			return error.get();
		}

		public String getStartTime() {
			return startTime.get();
		}

		public String getEndTime() {
			return endTime.get();
		}

	}

}
