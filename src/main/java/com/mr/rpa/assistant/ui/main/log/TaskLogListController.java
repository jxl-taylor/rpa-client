package com.mr.rpa.assistant.ui.main.log;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.ui.addtask.TaskAddController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
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

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class TaskLogListController implements Initializable {
	@FXML
	private TableView<TaskLog> tableView;
	@FXML
	private TableColumn<TaskLog, String> idCol;
	@FXML
	private TableColumn<TaskLog, Integer> statusCol;
	@FXML
	private TableColumn<TaskLog, String> errorCol;
	@FXML
	private TableColumn<TaskLog, java.util.Date> startTime;
	@FXML
	private TableColumn<TaskLog, java.util.Date> endTime;
	@FXML
	private AnchorPane rootPane;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initCol();
		tableView.setItems(DataHelper.getTaskLogList());
		tableView.setRowFactory(tv -> {
			TableRow<TaskLog> row = new TableRow<TaskLog>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					showLogDetail(row.getItem());
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
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		errorCol.setCellValueFactory(new PropertyValueFactory<>("error"));
		startTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
		endTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
	}

	private List<TaskLog> loadLogData() {
		return DataHelper.loadTaskLogList(GlobalProperty.getInstance().getSelectedTaskId().get());
	}

	@FXML
	private void handleTaskDeleteOption(ActionEvent event) {

	}

	@FXML
	private void handleTaskDetailOption(ActionEvent event) {
		//Fetch the selected row
		TaskLog selectedForDetail = tableView.getSelectionModel().getSelectedItem();
	}

	private void showLogDetail(TaskLogListController.TaskLog selectedForEdit){

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
		private final SimpleIntegerProperty status;
		private final SimpleStringProperty error;
		private final SimpleObjectProperty<java.util.Date> startTime;
		private final SimpleObjectProperty<java.util.Date> endTime;

		public TaskLog(String id, String taskId, Integer status, String error, java.util.Date startTime, java.util.Date endTime) {
			this.id = new SimpleStringProperty(id);
			this.taskId = new SimpleStringProperty(taskId);
			this.status = new SimpleIntegerProperty(status);
			this.error = new SimpleStringProperty(error);
			this.startTime = new SimpleObjectProperty<>(startTime);
			this.endTime = new SimpleObjectProperty<>(endTime);
		}

		public String getId() {
			return id.get();
		}

		public String getTaskId() {
			return taskId.get();
		}

		public int getStatus() {
			return status.get();
		}

		public String getError() {
			return error.get();
		}

		public Date getStartTime() {
			return startTime.get();
		}

		public Date getEndTime() {
			return endTime.get();
		}

	}

}
