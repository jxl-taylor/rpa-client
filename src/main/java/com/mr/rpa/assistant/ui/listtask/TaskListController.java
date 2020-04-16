package com.mr.rpa.assistant.ui.listtask;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.addtask.TaskAddController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.KeyValue;
import com.mr.rpa.assistant.util.Pair;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

@Log4j
public class TaskListController implements Initializable {
	@FXML
	private TableView<Task> tableView;
	@FXML
	private TableColumn<Task, Integer> seqCol;
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

	private JFXButton cancelBtn;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setTaskTableView(tableView);
		initCol();
		loadData();
		tableView.setItems(taskService.getUITaskList());
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
					loadLogData();
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
		globalProperty.setTaskListController(this);
	}

	private Stage getStage() {
		return (Stage) tableView.getScene().getWindow();
	}

	private void initCol() {
		seqCol.setCellValueFactory(new PropertyValueFactory<>("seq"));
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		cronCol.setCellValueFactory(new PropertyValueFactory<>("cron"));
		despCol.setCellValueFactory(new PropertyValueFactory<>("desp"));
		runningCol.setCellValueFactory(new PropertyValueFactory<>("running"));
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		successCountCol.setCellValueFactory(new PropertyValueFactory<>("successCount"));
		failCountCol.setCellValueFactory(new PropertyValueFactory<>("failCount"));
	}

	public void loadData() {
		taskService.loadUITaskList();
		tableView.setItems(taskService.getUITaskList());
	}

	private void loadLogData() {
		GlobalProperty.getInstance().getTaskHistoryController().loadTaskLog(null);
	}

	@FXML
	private void deleteAllTaskLog(ActionEvent event) {
		//Fetch the selected row
		Task selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
		if (selectedForDeletion == null) {
			AlertMaker.showErrorMessage("未选择任务", "请选择一个任务删除.");
			return;
		}

		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedForDeletion.getId());
				deleteAllTaskLog(task);
				AlertMaker.showSimpleAlert("清空日志", selectedForDeletion.getName() + " 操作成功.");
				loadLogData();
			} catch (Exception e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
			}

		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "情况日志",
				"确定清空该任务的所有日志么 " + selectedForDeletion.getName() + " ?", false);

	}

	public synchronized void deleteAllTaskLog(com.mr.rpa.assistant.data.model.Task task) {
		taskLogService.deleteTaskLogByTaskId(task.getId());
		loadData();
	}

	@FXML
	private void handleTaskDeleteOption(ActionEvent event) {
		//Fetch the selected row
		Task selectedForDeletion = tableView.getSelectionModel().getSelectedItem();
		if (selectedForDeletion == null) {
			AlertMaker.showErrorMessage("未选择任务", "请选择一个任务删除.");
			return;
		}
		if (CollectionUtil.isNotEmpty(taskService.queryTaskByNextTask(selectedForDeletion.getName()))) {
			AlertMaker.showErrorMessage("无法删除", "该任务被别的任务依赖.");
			return;
		}
		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				JobFactory.delete(taskService.queryTaskById(selectedForDeletion.getId()));
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
				return;
			}
			String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath();
			String jobPath = taskFileDir + File.separator + selectedForDeletion.getName();
			FileUtil.del(jobPath);
			taskService.deleteTask(selectedForDeletion.getId());
			taskLogService.deleteTaskLogByTaskId(selectedForDeletion.getId());
			AlertMaker.showSimpleAlert("删除任务", selectedForDeletion.getName() + " 删除成功.");
			taskService.removeTask(selectedForDeletion);
			taskLogService.getUITaskLogList().clear();
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
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader()
				.getResource("assistant/ui/addtask/add_task.fxml"), "编辑任务", null);

		TaskAddController controller = (TaskAddController) pair.getObject2();
		controller.inflateUI(selectedForEdit);
		Stage stage = pair.getObject1();
		stage.setOnHiding((e) -> {
			handleRefresh(new ActionEvent());
		});

		stage.setOnCloseRequest((event) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"));
			Pair<Stage, Object> cronPair = AssistantUtil.getWindow(getClass()
					.getClassLoader().getResource("assistant/ui/addtask/cron_setting.fxml"));
			if (cronPair != null) cronPair.getObject1().close();
		});

	}

	@FXML
	private void startTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedTask.getId());
		if (task.isRunning()) {
			AlertMaker.showSimpleAlert("开启任务", "不能重复开启");
			return;
		}

		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				startTask(task);
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
				throw new RuntimeException(e);
			}

			AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "启动任务", selectedTask.getId() + " 已开启");

		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "开启任务", "确定开启该任务么 " + task.getName() + " ?", false);

	}

	public synchronized void startTask(com.mr.rpa.assistant.data.model.Task task) throws SchedulerException {
		if (task.isRunning()) return;
		task.setRunning(true);
		JobFactory.add(task);
		taskService.updateTaskRunning(task.getId(), true);
		loadData();
	}

	@FXML
	private void endTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("停止任务", "任务已经停止");
			return;
		}

		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				endTask(task);
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
				throw new RuntimeException(e);
			}
			AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "停止任务", selectedTask.getId() + " 已停止");

		});

		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "停止任务", "确定停止该任务么" + task.getName() + " ?", false);
	}

	public synchronized void endTask(com.mr.rpa.assistant.data.model.Task task) throws SchedulerException {
		if (!task.isRunning()) return;
		JobFactory.delete(task);
		taskService.updateTaskRunning(task.getId(), false);
		loadData();
	}

	@FXML
	private void resumeTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("恢复执行", "任务还没启动");
			return;
		}

		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_PAUSE) {
			AlertMaker.showSimpleAlert("恢复执行", "任务未暂停");
			return;
		}

		try {
			resumeTask(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
			return;
		}
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "恢复执行", selectedTask.getName() + " 已恢复");
	}

	public synchronized void resumeTask(com.mr.rpa.assistant.data.model.Task task) throws SchedulerException {
		if (!task.isRunning()) return;
		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_PAUSE) return;
		task.setStatus(SystemContants.TASK_RUNNING_STATUS_RUN);
		JobFactory.resume(task);
		taskService.updateTaskStatus(task.getId(), SystemContants.TASK_RUNNING_STATUS_RUN);
		loadData();
	}

	@FXML
	private void pauseTask(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedTask.getId());
		if (!task.isRunning()) {
			AlertMaker.showSimpleAlert("暂停执行", "任务还没启动");
			return;
		}

		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_RUN) {
			AlertMaker.showSimpleAlert("暂停执行", "任务已暂停");
			return;
		}

		try {
			pauseTask(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
			return;
		}
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "暂停执行", selectedTask.getName() + " 已暂停 ");
	}

	public synchronized void pauseTask(com.mr.rpa.assistant.data.model.Task task) throws SchedulerException {
		if (!task.isRunning()) return;
		if (task.getStatus() != SystemContants.TASK_RUNNING_STATUS_RUN) return;
		task.setStatus(SystemContants.TASK_RUNNING_STATUS_PAUSE);
		JobFactory.pause(task);
		taskService.updateTaskStatus(task.getId(), SystemContants.TASK_RUNNING_STATUS_PAUSE);
		loadData();
	}

	@FXML
	private void handleRefresh(ActionEvent event) {
		taskLogService.getUITaskLogList().clear();
		loadData();
	}

	@FXML
	private void triggerByManual(ActionEvent event) {
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		com.mr.rpa.assistant.data.model.Task task = taskService.queryTaskById(selectedTask.getId());
		try {
			triggerByManual(task);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
			return;
		}
		AlertMaker.showMaterialDialog(rootPane, contentPane, new ArrayList<>(), "手动触发", selectedTask.getName() + " 触发成功");
		loadLogData();
	}

	public synchronized void triggerByManual(com.mr.rpa.assistant.data.model.Task task) throws SchedulerException {
		JobFactory.triggerByManual(task.getId());
	}

	@FXML
	private void showResult(ActionEvent event) {
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		Task selectedTask = tableView.getSelectionModel().getSelectedItem();
		if (selectedTask == null) {
			AlertMaker.showErrorMessage("未选择任务", "请先选择一个任务.");
			return;
		}
		List<KeyValue> keyValues = JSON.parseArray(selectedTask.params.get())
				.toJavaList(KeyValue.class);
		String code = "";
		for (KeyValue kv : keyValues) {
			kv.getObject1().equals(SystemContants.RESULT_PATH_CODE);
			code = kv.getObject2();
		}

		if (StringUtils.isNotBlank(code)) {
			String resultPath = String.format(sysConfig.getRunResultPath(), code);
			if (FileUtil.exist(resultPath) && FileUtil.isDirectory(resultPath)) {
				try {
					Desktop.getDesktop().open(new File(resultPath));
				} catch (IOException e) {
					log.error(e);
					AlertMaker.showErrorMessage(e);
				}
				return;
			}
		}

		AlertMaker.showErrorMessage("结果查看", "CODE未设置，请自行查找运行结果");
		try {
			Desktop.getDesktop().open(new File(sysConfig.getDefaultResultPath()));
		} catch (IOException e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
		}
		return;

	}

	public static class Task {

		private final SimpleIntegerProperty seq;
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

		public Task(int seq, String id, String name, String mainTask, String desp, String params, String nextTask, Boolean running, Integer status, String cron, Integer successCount, Integer failCount) {
			this.seq = new SimpleIntegerProperty(seq);
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

		public int getSeq() {
			return seq.get();
		}

		public SimpleIntegerProperty seqProperty() {
			return seq;
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
