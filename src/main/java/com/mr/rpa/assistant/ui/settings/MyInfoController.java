package com.mr.rpa.assistant.ui.settings;

import com.jfoenix.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.log4j.Logger;

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
	private JFXTextField connectTime;
	@FXML
	private JFXTextField connectStatus;
	@FXML
	private JFXTextField duration;

	private final static Logger LOGGER = Logger.getLogger(MyInfoController.class.getName());

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		userNick.setText("管理员");
		expireTime.setText("2021-01-01");
		username.setText("admin");
		connectTime.setText("2020-02-05 15:00:00");
		connectStatus.setText("未连接");
		duration.setText("10小时20分钟");
	}
}
