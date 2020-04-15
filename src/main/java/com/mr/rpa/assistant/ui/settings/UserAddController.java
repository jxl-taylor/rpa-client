package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.ui.addtask.CronSettingController;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Log4j
public class UserAddController implements Initializable {

	@FXML
	private JFXTextField id;
	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;
	@FXML
	private JFXPasswordField password2;
	@FXML
	private JFXTextField nick;
	@FXML
	private JFXTextField mail;
	@FXML
	private JFXTextField phone;

	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;

	private SimpleBooleanProperty isInEditMode = new SimpleBooleanProperty();

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	private UserService userService = ServiceFactory.getService(UserService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		id.visibleProperty().bind(isInEditMode);
	}

	@FXML
	private void addUser(ActionEvent event) {
		SysConfig sysConfig = globalProperty.getSysConfig();
		if (!checkInput()) return;
		if (isInEditMode.getValue()) {
			handleEditOperation();
			return;
		}
		id.setText(UUID.randomUUID().toString().replace("-", ""));
		User user = new User(id.getText(), username.getText(), password.getText(),
				nick.getText(), phone.getText(), mail.getText(),
				false,
				sysConfig.getAdminUsername(), sysConfig.getAdminUsername(),
				new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
		userService.addUser(user);
		AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "新增用户", nick.getText() + " 已添加");
		clearEntries();
		MyInfoController.queryUsers();
		closeWindow();
	}

	private boolean checkInput() {
		if (StringUtils.isBlank(username.getText())) {
			username.getStyleClass().add("wrong-credentials");
			return false;
		}
		if (StringUtils.isBlank(nick.getText())) {
			nick.getStyleClass().add("wrong-credentials");
			return false;
		}
		if (StringUtils.isBlank(password.getText())) {
			password.getStyleClass().add("wrong-credentials");
			return false;
		}
		if (!password.getText().equals(password2.getText())) {
			password.getStyleClass().add("wrong-credentials");
			password2.getStyleClass().add("wrong-credentials");
			return false;
		}
		if (StringUtils.isBlank(phone.getText()) && StringUtils.isBlank(mail.getText())) {
			phone.getStyleClass().add("wrong-credentials");
			mail.getStyleClass().add("wrong-credentials");
			return false;
		}
		String mailRegex = "\\w+@\\w+(\\.\\w{2,3})*\\.\\w{2,3}";
		if (StringUtils.isNotEmpty(mail.getText()) && !mail.getText().matches(mailRegex)) {
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "邮箱格式不正确.");
			mail.getStyleClass().add("wrong-credentials");
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

	public void inflateUI(String sUsername) {
		User user = userService.getUserListByUsername(sUsername).get(0);
		id.setText(user.getId());
		username.setText(user.getUsername());
		password.setText(user.getPassword());
		password2.setText(user.getPassword());
		nick.setText(user.getNick());
		mail.setText(user.getMail());
		phone.setText(user.getPhone());
		id.setEditable(false);

		isInEditMode.setValue(Boolean.TRUE);
	}

	private void clearEntries() {
		id.clear();
		username.clear();
		password.clear();
		password2.clear();
		nick.clear();
		mail.clear();
		phone.clear();
	}

	private void handleEditOperation() {
		User user = userService.getUserListByUsername(username.getText()).get(0);
		user.setPassword(password.getText());
		user.setNick(nick.getText());
		user.setMail(mail.getText());
		user.setPhone(phone.getText());
		user.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				userService.updateUser(user);
				AlertMaker.showSimpleAlert("Success", "修改成功");
				MyInfoController.queryUsers();
				closeWindow();
			} catch (Exception e) {
				log.error(e);
				AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "Failed", "修改失败");
			}
		});
		JFXButton cancelBtn = new JFXButton("取消");
		cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			rootPane.getChildren().get(0).setEffect(null);
		});
		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				Lists.newArrayList(confirmBtn, cancelBtn), "修改用户",
				"确定保存修改么?", false);
	}
}
