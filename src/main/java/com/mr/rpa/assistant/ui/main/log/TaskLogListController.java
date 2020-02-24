package com.mr.rpa.assistant.ui.main.log;

import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TaskLogListController implements Initializable {

	private final static Logger LOGGER = Logger.getLogger(TaskLogListController.class);

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
				if(!row.isEmpty()){
					if (event.getClickCount() == 2) {
						showLogDetail(row.getItem());
					}
					String taskLogId = row.getItem().getId();
					SimpleStringProperty selectedTaskLogId = globalProperty.getSelectedTaskLogId();
					if(!taskLogId.equalsIgnoreCase(selectedTaskLogId.get())) globalProperty.getLogTextCollector().clearSelectLog();
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
	private void handleTaskDeleteOption(ActionEvent event) {

	}

	@FXML
	private void handleTaskDetailOption(ActionEvent event) {
		//Fetch the selected row
		TaskLog selectedForDetail = tableView.getSelectionModel().getSelectedItem();
	}

	private void showLogDetail(TaskLogListController.TaskLog selectedForEdit) {

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
		private final SimpleStringProperty taskId;
		private final SimpleStringProperty status;
		private final SimpleStringProperty error;
		private final SimpleStringProperty startTime;
		private final SimpleStringProperty endTime;

		public TaskLog(String id, String taskId, Integer status, String error, java.util.Date startTime, java.util.Date endTime) {
			this.id = new SimpleStringProperty(id);
			this.taskId = new SimpleStringProperty(taskId);
			if (status == 0) {
				this.status = new SimpleStringProperty("正在执行");
			} else if (status == 1) {
				this.status = new SimpleStringProperty("执行成功");
			} else if (status == 2) {
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
