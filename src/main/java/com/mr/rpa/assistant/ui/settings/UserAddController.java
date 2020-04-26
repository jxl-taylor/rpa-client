package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.DatePattern;
import com.google.common.collect.Lists;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

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
	private JFXTextField status;
	@FXML
	private JFXTextField createTime;
	@FXML
	private JFXTextField updateTime;

	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;
	@FXML
	private VBox vBox;

	private SimpleBooleanProperty isInEditMode = new SimpleBooleanProperty();

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	private UserService userService = ServiceFactory.getService(UserService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		vBox.getChildren().removeAll(id, status, createTime, updateTime);
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
		try {
			if (userService.getUserListByUsername(username.getText()).size() > 0) {
				AlertMaker.showErrorMessage("保存用户", String.format("用户[%s]已存在", username.getText()));
				return;
			}
			userService.addUser(user);
			clearEntries();
			MyInfoController.queryUsers();
			AlertMaker.showSimpleAlert("新增用户", "新增成功.");
			closeWindow();
		} catch (Exception e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
		}

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
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "两次输入密码不一致.");
			return false;
		}
		if (StringUtils.isBlank(phone.getText()) && StringUtils.isBlank(mail.getText())) {
			phone.getStyleClass().add("wrong-credentials");
			mail.getStyleClass().add("wrong-credentials");
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(), "输入有误", "电话与邮箱不能同时为空.");
			return false;
		}

		if (StringUtils.isNotEmpty(mail.getText()) && !mail.getText().matches(SystemContants.MAIL_REGEX)) {
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
		status.setText(user.isLocking() ? "已禁用" : "已启用");
		createTime.setText(DatePattern.NORM_DATETIME_FORMAT.format(user.getCreateTime().getTime()));
		updateTime.setText(DatePattern.NORM_DATETIME_FORMAT.format(user.getUpdateTime().getTime()));
		vBox.getChildren().add(0, id);
		vBox.getChildren().add(7, status);
		vBox.getChildren().add(8, createTime);
		vBox.getChildren().add(9, updateTime);
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
		status.clear();
		createTime.clear();
		updateTime.clear();
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
				AlertMaker.showErrorMessage(e);
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
