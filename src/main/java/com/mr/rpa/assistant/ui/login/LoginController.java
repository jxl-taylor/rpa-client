package com.mr.rpa.assistant.ui.login;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
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

	private static final String RPA_CONTROL_CENTER = "http://microrule.com/";

	@FXML
	private AnchorPane loginPane;

	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;
	@FXML
	private Hyperlink applyLink;
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
		globalProperty.doAfterLogin();
		StackPane rootPane = GlobalProperty.getInstance().getRootPane();
		AlertMaker.showMaterialDialog(rootPane,
				rootPane.getChildren().get(0),
				new ArrayList<>(), "", "登录成功");
	}

	@FXML
	private void handleCancelButtonAction(ActionEvent event) {
		closeStage();
	}

	@FXML
	private void linkToApply(ActionEvent event){
		try {
			Desktop.getDesktop().browse(new URI(RPA_CONTROL_CENTER));
			applyLink.setVisited(false);
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
	private void closeStage() {
		StackPane rootPane = GlobalProperty.getInstance().getRootPane();
		rootPane.getChildren().get(0).setEffect(null);
		((Stage) username.getScene().getWindow()).close();
	}
}
