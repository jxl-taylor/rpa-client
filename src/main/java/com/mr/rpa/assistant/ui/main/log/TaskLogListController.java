package com.mr.rpa.assistant.ui.main.log;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.ui.addtask.TaskLogDetailController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
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
import org.apache.commons.lang3.time.DateFormatUtils;
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
	private StackPane rootPane;

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		initCol();
		tableView.setItems(taskLogService.getUITaskLogList());
		tableView.setRowFactory(tv -> {
			TableRow<TaskLog> row = new TableRow<TaskLog>();
			row.setOnMouseClicked(event -> {
				if (!row.isEmpty()) {
					if (event.getClickCount() == 2) {
						showLogDetail(row.getItem());
					}
					String taskLogId = row.getItem().getTaskLogId();
					globalProperty.getSelectedTaskLogId().set(taskLogId);
				}
			});
			return row;
		});

		tableView.minHeightProperty().bind(globalProperty.getLogListHeight());
		tableView.maxHeightProperty().bind(globalProperty.getLogListHeight());
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
		Integer status = GlobalProperty.getInstance().getTaskHistoryController().getStatusChoice();
		if (status == -1) {
			return taskLogService.loadUITaskLogList(GlobalProperty.getInstance().getSelectedTaskId().get());
		}
		return taskLogService.loadUITaskLogList(GlobalProperty.getInstance().getSelectedTaskId().get(), status);
	}

	@FXML
	private void handleDelete(ActionEvent event) {
		//Fetch the selected row
		TaskLog selectedForDetail = tableView.getSelectionModel().getSelectedItem();
		if (selectedForDetail == null) {
			AlertMaker.showErrorMessage("未选择任务日志", "请选择一条记录.");
			return;
		}
		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				taskLogService.deleteTaskLogByLogId(selectedForDetail.getTaskLogId());
				AlertMaker.showSimpleAlert("删除", "删除成功.");
				loadLogData();
			} catch (Exception e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
			}
		});

		JFXButton cancelBtn = new JFXButton("取消");
		cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			StackPane rootPane = GlobalProperty.getInstance().getRootPane();
			rootPane.getChildren().get(0).setEffect(null);
		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "删除日志",
				String.format("确定删除日志[id=%s]么?", selectedForDetail.getId()), false);

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
			com.mr.rpa.assistant.data.model.TaskLog taskLog = taskLogService.loadTaskLogById(selectedForDetail.getTaskLogId());
			if (taskLog.getStatus() == SystemContants.TASK_LOG_STATUS_RUNNING) {
				AlertMaker.showErrorMessage("重新执行", "任务运行中，请稍等再试.");
				return;
			}
			JobFactory.trigger(taskLog);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
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
			AssistantUtil.setStageIcon(stage);

			stage.setResizable(false);
			stage.setFullScreen(false);

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

		private final SimpleIntegerProperty id;
		private final SimpleStringProperty taskLogId;
		private final SimpleStringProperty taskId;
		private final SimpleStringProperty status;
		private final SimpleStringProperty error;
		private final SimpleStringProperty startTime;
		private final SimpleStringProperty endTime;

		public TaskLog(Integer id, String taskLogId, String taskId, Integer status, String error, java.util.Date startTime, java.util.Date endTime) {
			this.id = new SimpleIntegerProperty(id);
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

		public Integer getId() {
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
