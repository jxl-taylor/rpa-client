package com.mr.rpa.assistant.ui.addtask;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.KeyValue;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Log4j
public class TaskAddController implements Initializable {

	@FXML
	private JFXTextField id;
	@FXML
	private JFXTextField name;
	@FXML
	private JFXComboBox<String> mainTask;
	@FXML
	private JFXTextField cron;
	@FXML
	private JFXTextArea desp;
	@FXML
	private JFXComboBox<String> nextTask;
	@FXML
	private StackPane rootPane;
	@FXML
	private VBox vFormBox;

	@FXML
	private HBox kjbName;
	@FXML
	private JFXButton uploadFileButton;
	@FXML
	private JFXButton uploadDirButton;

	@FXML
	private AnchorPane mainContainer;

	private LinkedHashMap<Object, HBox> paramDeleteMap = Maps.newLinkedHashMap();
	private LinkedHashMap<Object, HBox> paramConfirmMap = Maps.newLinkedHashMap();

	private SimpleBooleanProperty isInEditMode = new SimpleBooleanProperty();

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	private ObservableList<String> nextTaskItems = FXCollections.observableArrayList();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		id.visibleProperty().bind(isInEditMode);
		final FileChooser fileChooser = new FileChooser();
		uploadFileButton.setOnAction((final ActionEvent e) -> {
			configureFileChooser(fileChooser);
			File file = fileChooser.showOpenDialog(uploadFileButton.getScene().getWindow());
			if (file != null) {
				String taskName = file.getName().substring(0, file.getName().indexOf("."));
				String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath();
				if (taskDao.queryTaskByName(taskName) != null) {
					log.error(String.format("bot[%s] 已经存在", taskName));
					AlertMaker.showErrorMessage("上传Bot", String.format("bot[%s] 已经存在", taskName));
					return;
				}

				FileUtil.del(taskFileDir + File.separator + name);
				name.setText(taskName);
				FileUtil.copy(file.getAbsolutePath(), taskFileDir
						+ File.separator
						+ taskName
						+ File.separator
						+ file.getName(), true);
				mainTask.getItems().add(file.getName());
				mainTask.setValue(file.getName());
			}
		});
		uploadDirButton.setOnAction((final ActionEvent e) -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("选择文件夹");
			File directory = directoryChooser.showDialog(new Stage());
			if (directory != null) {
				String taskFileDir = GlobalProperty.getInstance().getSysConfig().getTaskFilePath();
				if (taskDao.queryTaskByName(directory.getName()) != null) {
					log.error(String.format("bot[%s] 已经存在", directory.getName()));
					AlertMaker.showErrorMessage("上传Bot", String.format("bot[%s] 已经存在", directory.getName()));
					return;
				}
				FileUtil.del(taskFileDir + File.separator + directory.getName());
				name.setText(directory.getName());
				FileUtil.copy(directory.getAbsolutePath(), taskFileDir + File.separator
						, true);
				//添加主任务选项
				String[] fileNames = directory.list();
				for (int i = 0; i < fileNames.length; i++) {
					mainTask.getItems().add(fileNames[i]);
				}
			}
		});

		//添加依赖任务选项
		nextTask.setItems(nextTaskItems);
		nextTaskItems.clear();
		nextTaskItems.add("");
		nextTaskItems.addAll(taskDao.queryTaskList()
				.stream()
				.map(item -> item.getName())
				.collect(Collectors.toList()));


		//添加主任务选项
		mainTask.getItems().clear();
	}

	@FXML
	private void addParam(ActionEvent event) {
		HBox paramBox = new HBox();
		paramBox.setSpacing(5);

		JFXTextField keyField = new JFXTextField();
		keyField.setLabelFloat(false);
		keyField.setPromptText("参数名");
		paramBox.getChildren().add(keyField);

		JFXTextField valueField = new JFXTextField();
		valueField.setLabelFloat(false);
		valueField.setPromptText("参数值");
		paramBox.getChildren().add(valueField);
		HBox.setHgrow(valueField, Priority.ALWAYS);

		//确定、删除事件
		JFXButton confirmButton = new JFXButton("确定");
		confirmButton.setOnAction((final ActionEvent e) -> {
			HBox selectedHBox = paramConfirmMap.get(e.getSource());
			JFXTextField kField = (JFXTextField) selectedHBox.getChildren().get(0);
			if (StringUtils.isBlank(kField.getText())) {
				kField.getStyleClass().add("wrong-credentials");
				kField.requestFocus();
				throw new RuntimeException("Key不能为空");
			}
			Label keyLabel = new Label(kField.getText());
			keyLabel.setPrefWidth(100);
			keyLabel.setStyle("-fx-text-fill: -fx-secondary; -fx-alignment: CENTER; -fx-padding: 10px 0 0 0;");
			selectedHBox.getChildren().remove(0);
			selectedHBox.getChildren().remove(e.getSource());
			selectedHBox.getChildren().add(0, keyLabel);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					selectedHBox.getChildren().get(1).requestFocus();
				}
			});
		});
		paramBox.getChildren().add(confirmButton);
		paramConfirmMap.put(confirmButton, paramBox);

		JFXButton deleteButton = new JFXButton("删除");
		deleteButton.setOnAction((final ActionEvent e) -> {
			HBox selectedHBox = paramDeleteMap.get(e.getSource());
			vFormBox.getChildren().remove(selectedHBox);
			paramDeleteMap.remove(e.getSource());
		});
		paramBox.getChildren().add(deleteButton);
		paramDeleteMap.put(deleteButton, paramBox);

		vFormBox.getChildren().add(vFormBox.getChildren().size() - 2, paramBox);
	}

	@FXML
	private void addTask(ActionEvent event) {
		kjbName.getChildren().clear();
		kjbName.getChildren().addAll(name, uploadFileButton, uploadDirButton);
		String taskName = StringUtils.trimToEmpty(name.getText());
		String mainTaskName = StringUtils.trimToEmpty(mainTask.getValue());
		String taskCron = StringUtils.trimToEmpty(cron.getText());
		String taskDesp = StringUtils.trimToEmpty(desp.getText());

		if (isInEditMode.getValue()) {
			handleEditOperation();
			return;
		}
		if (!checkInput(taskName, mainTaskName, taskCron, taskDesp)) return;
		if (taskDao.queryTaskByName(taskName) != null) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(),
					"输入有误", "Job已存在，请重新选择.");
			return;
		}

		id.setText(UUID.randomUUID().toString().replace("-", ""));
		Task task = new Task(id.getText(), name.getText(), mainTaskName, desp.getText(), converParamToString(), nextTask.getValue(),
				false, SystemContants.TASK_RUNNING_STATUS_RUN, cron.getText(),
				0, 0);
		boolean result = taskDao.insertNewTask(task);
		if (result) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增任务", taskName + " 已添加");
			clearEntries();
			taskDao.loadTaskList();
			closeWindow();
		} else {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增失败", "请检查输入");
		}
	}

	private boolean checkInput(String taskName, String mainTaskName, String taskCron, String taskDesp) {
		if (taskName.isEmpty() || mainTaskName.isEmpty()|| taskDesp.isEmpty() || taskCron.isEmpty()) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "任务名称、描述、CRON不能为空.");
			return false;
		}
		if (!CronExpression.isValidExpression(taskCron)) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "CRON表达式无效.");
			return false;
		}
		return true;
	}

	@FXML
	private void cancel(ActionEvent event) {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) rootPane.getScene().getWindow();
		stage.close();
	}

	public void inflateUI(TaskListController.Task task) {
		id.setText(task.getId());
		name.setText(task.getName());
		mainTask.setValue(task.getMainTask());
		cron.setText(task.getCron());
		desp.setText(task.getDesp());
		convertStringToParam(task.getParams());
		nextTask.setValue(task.getNextTask());
		id.setEditable(false);
		kjbName.getChildren().clear();
		kjbName.getChildren().add(name);

		//依赖任务不能是自己
		String selectedTaskId = GlobalProperty.getInstance().getSelectedTaskId().getValue();
		Task selectTask = taskDao.queryTaskById(selectedTaskId);
		String taskName = selectTask.getName();

		File mainTaskDir = new File(GlobalProperty.getInstance().getSysConfig().getTaskFilePath()
				+ File.separator
				+ taskName);
		String[] fileNames = mainTaskDir.list();
		for (int i = 0; i < fileNames.length; i++) {
			mainTask.getItems().add(fileNames[i]);
		}

		nextTaskItems.remove(taskName);

		isInEditMode.setValue(Boolean.TRUE);
	}

	private void clearEntries() {
		id.clear();
		name.clear();
		mainTask.setValue("");
		cron.clear();
		desp.clear();
		paramConfirmMap.clear();
		vFormBox.getChildren().removeAll(paramDeleteMap.values());
		paramDeleteMap.clear();
	}

	private void handleEditOperation() {
		TaskListController.Task task = new TaskListController.Task(id.getText(), name.getText(), mainTask.getValue(),
				desp.getText(), converParamToString(),
				nextTask.getValue(), false, 0, cron.getText(), 0, 0);
		if (!checkInput(task.getName(), mainTask.getValue(), task.getCron(), task.getDesp())) return;
		try {
			Task taskModel = taskDao.queryTaskById(task.getId());
			taskModel.setMainTask(mainTask.getValue());
			taskModel.setCron(task.getCron());
			taskModel.setDesp(task.getDesp());
			taskModel.setParams(task.getParams());
			taskModel.setNextTask(nextTask.getValue());
			JobFactory.update(taskModel);
		} catch (SchedulerException e) {
			log.error(e);
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
		}
		if (taskDao.updateTask(task)) {
			AlertMaker.showSimpleAlert("Success", "修改成功");
			closeWindow();
		} else {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
		}
	}

	private static void configureFileChooser(
			final FileChooser fileChooser) {
		fileChooser.setTitle("选择任务");
		fileChooser.setInitialDirectory(
				new File(System.getProperty("user.home"))
		);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("所有任务", "*.*"),
				new FileChooser.ExtensionFilter("kjb", "*.kjb"),
				new FileChooser.ExtensionFilter("ktr", "*.ktr")
		);
	}

	public String converParamToString() {
		List<KeyValue> keyValues = paramDeleteMap.values().stream().map(pBox -> {
			List<Node> nodes = pBox.getChildren().stream().filter(node -> !(node instanceof JFXButton)).collect(Collectors.toList());
			KeyValue keyValue = null;
			if (nodes.get(0) instanceof Label) {
				keyValue = new KeyValue(((Label) nodes.get(0)).getText(), ((JFXTextField) nodes.get(1)).getText());
				if (StringUtils.isBlank(keyValue.getObject1())) {
					((JFXTextField) nodes.get(1)).getStyleClass().add("wrong-credentials");
					((JFXTextField) nodes.get(1)).requestFocus();
					throw new RuntimeException("Value不能为空");
				}
			} else {
				keyValue = new KeyValue(((JFXTextField) nodes.get(0)).getText(), ((JFXTextField) nodes.get(1)).getText());
				if (StringUtils.isBlank(keyValue.getObject1())) {
					((JFXTextField) nodes.get(0)).getStyleClass().add("wrong-credentials");
					((JFXTextField) nodes.get(0)).requestFocus();
					throw new RuntimeException("Key不能为空");
				}
				if (StringUtils.isBlank(keyValue.getObject1())) {
					((JFXTextField) nodes.get(1)).getStyleClass().add("wrong-credentials");
					((JFXTextField) nodes.get(1)).requestFocus();
					throw new RuntimeException("Value不能为空");
				}
			}

			return keyValue;
		}).collect(Collectors.toList());
		return JSON.toJSONString(keyValues);
	}

	public void convertStringToParam(String paramString) {
		vFormBox.getChildren().removeAll(paramDeleteMap.values());
		List<KeyValue> keyValues = JSON.parseArray(paramString).toJavaList(KeyValue.class);
		keyValues.forEach(keyValue -> {
			HBox paramBox = new HBox();
			paramBox.setSpacing(5);

			Label keyLabel = new Label(keyValue.getObject1());
			keyLabel.setPrefWidth(100);
			keyLabel.setStyle("-fx-text-fill: -fx-secondary; -fx-alignment: CENTER; -fx-padding: 10px 0 0 0;");
			paramBox.getChildren().add(keyLabel);

			JFXTextField valueField = new JFXTextField();
			valueField.setLabelFloat(false);
			valueField.setText(keyValue.getObject2());
			paramBox.getChildren().add(valueField);
			HBox.setHgrow(valueField, Priority.ALWAYS);

			//删除事件
			JFXButton deleteButton = new JFXButton("删除");
			deleteButton.setOnAction((final ActionEvent e) -> {
				HBox selectedHBox = paramDeleteMap.get(e.getSource());
				vFormBox.getChildren().remove(selectedHBox);
			});
			paramBox.getChildren().add(deleteButton);
			paramDeleteMap.put(deleteButton, paramBox);

			vFormBox.getChildren().add(vFormBox.getChildren().size() - 2, paramBox);
		});

	}
}
