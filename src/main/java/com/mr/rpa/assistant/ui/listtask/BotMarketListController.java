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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Log4j
public class BotMarketListController implements Initializable {
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
	private TableColumn<Task, CheckBox> registeredCol;
	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		initCol();
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
		registeredCol.setCellValueFactory(new PropertyValueFactory<>("checkBox"));
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

		private CheckBox checkBox = new CheckBox();

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
