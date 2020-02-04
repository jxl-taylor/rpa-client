package com.mr.rpa.assistant.ui.login;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.ui.settings.Preferences;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController implements Initializable {

	private final static Logger LOGGER = LogManager.getLogger(LoginController.class.getName());

	@FXML
	private AnchorPane loginPane;

	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;

	Preferences preference;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		preference = Preferences.getPreferences();
		password.addEventHandler(ActionEvent.ACTION, GlobalProperty.getInstance().getAfterLoginEventHandler());
	}

	@FXML
	private void handleLoginButtonAction(ActionEvent event) {
		String uname = StringUtils.trimToEmpty(username.getText());
		String pword = DigestUtils.shaHex(password.getText());

		if (uname.equals(preference.getUsername()) && pword.equals(preference.getPassword())) {
			closeStage();
			setAfterLogin();
			LOGGER.log(Level.INFO, "User successfully logged in {}", uname);
		} else {
			username.getStyleClass().add("wrong-credentials");
			password.getStyleClass().add("wrong-credentials");
		}
	}

	private void setAfterLogin() {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setTitle(username.getText());
		StackPane rootPane = GlobalProperty.getInstance().getRootPane();
		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				new ArrayList<>(), "登录", "登录成功");
	}

	@FXML
	private void handleCancelButtonAction(ActionEvent event) {
		closeStage();
	}

	private void closeStage() {
		((Stage) username.getScene().getWindow()).close();
	}
}
