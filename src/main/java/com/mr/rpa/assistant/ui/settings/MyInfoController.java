package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.jfoenix.controls.*;
import de.schlichtherle.license.LicenseContent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.commons.lang3.time.DateFormatUtils;
import java.net.URL;
import java.util.Date;
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

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		LicenseContent licenseContent = globalProperty.getLicenseContent();
		userNick.setText("管理员");
		expireTime.setText(DateFormatUtils.format(licenseContent.getNotAfter(), "yyyy-MM-dd"));
		username.setText("admin");
		connectTime.setText("");
		connectStatus.setText("未连接");
		duration.textProperty().bind(globalProperty.getRunningDuration());

	}

}
