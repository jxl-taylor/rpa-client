package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.addtask.CronSettingController;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.*;
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
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
public class RegisterController implements Initializable {

	private String mac;

	@FXML
	private JFXTextField companyName;
	@FXML
	private JFXTextField companyAddress;
	@FXML
	private JFXTextField applicant;
	@FXML
	private JFXTextField applyPhone1;
	@FXML
	private JFXTextField applyPhone2;
	@FXML
	private JFXTextField applyMail;

	@FXML
	private StackPane rootPane;

	@FXML
	private ScrollPane mainContainer;

	private SysConfigService configService = ServiceFactory.getService(SysConfigService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try {
			mac = CommonUtil.getLocalMac();
			SysConfig sysConfig = configService.query();
		} catch (Exception e) {
			log.error(e);
		}
	}

	@FXML
	private void save(ActionEvent event) {
		closeWindow();
	}

	@FXML
	private void cancel(ActionEvent event) {
		closeWindow();
	}

	private void closeWindow() {
		Stage stage = (Stage) rootPane.getScene().getWindow();
		stage.close();
	}
}
