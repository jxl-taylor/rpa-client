package com.mr.rpa.assistant.ui.settings;

import com.jfoenix.controls.*;
import com.mr.rpa.assistant.data.model.User;
import de.schlichtherle.license.LicenseContent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.commons.lang3.time.DateFormatUtils;
import java.net.URL;
import java.util.ResourceBundle;

public class MyInfoController implements Initializable {

	@FXML
	private JFXTextField userNick;
	@FXML
	private JFXTextField expireTime;
	@FXML
	private JFXTextField username;

	@FXML
	private JFXTextField duration;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		LicenseContent licenseContent = globalProperty.getLicenseContent();
		expireTime.setText(DateFormatUtils.format(licenseContent.getNotAfter(), "yyyy-MM-dd"));
		duration.textProperty().bind(globalProperty.getRunningDuration());
		globalProperty.setMyInfoController(this);
	}

	public void initCurrentUser(){
		User currentUser = globalProperty.getCurrentUser();
		userNick.setText(currentUser.getNick());
		username.setText(currentUser.getUsername());
	}
}
