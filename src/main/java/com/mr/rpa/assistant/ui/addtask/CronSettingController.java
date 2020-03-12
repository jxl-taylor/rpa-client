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
public class CronSettingController implements Initializable {

	@FXML
	private AnchorPane mainContainer;

	@FXML
	private JFXTabPane cronTabPane;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initComponents();
	}

	private void initComponents() {
		cronTabPane.tabMinWidthProperty().bind(cronTabPane.widthProperty().divide(cronTabPane.getTabs().size()).subtract(3));

	}

}
