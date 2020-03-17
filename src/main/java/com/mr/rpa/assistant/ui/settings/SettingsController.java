package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.data.model.SysConfig;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.job.HeartBeat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.export.DatabaseExporter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@Log4j
public class SettingsController implements Initializable {

	@FXML
	private JFXTextField mailServerName;
	@FXML
	private JFXTextField mailSmtpPort;
	@FXML
	private JFXTextField mailEmailAddress;

	private final static Logger LOGGER = Logger.getLogger(DatabaseHandler.class.getName());
	@FXML
	private JFXPasswordField mailEmailPassword;
	@FXML
	private JFXCheckBox mailSslCheckbox;
	@FXML
	private JFXTextField taskFilePath;
	@FXML
	private JFXTextField logPath;
	@FXML
	private JFXTextField controlServer;
	@FXML
	private JFXTextField connectDuration;
	@FXML
	private JFXTextField connectStatus;
	@FXML
	private JFXTextField dbPath;
	@FXML
	private JFXTextField miniteErrorLimit;
	@FXML
	private JFXTextField runningLimit;
	@FXML
	private JFXSpinner progressSpinner;

	@FXML
	private JFXTabPane settingTabPane;

	private SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initData();
		initComponents();
	}

	private void initComponents() {
		settingTabPane.tabMinWidthProperty().bind(settingTabPane.widthProperty().divide(settingTabPane.getTabs().size()).subtract(15));

	}

	@FXML
	private void testConnection(ActionEvent event){
		HeartBeat heartBeat = new HeartBeat();
		try {
			if(StringUtils.isBlank(controlServer.getText())) {
				AlertMaker.showErrorMessage("测试连接", "控制中心地址为空，请输入地址");
				return;
			}
			if(heartBeat.action(controlServer.getText())){
				AlertMaker.showSimpleAlert("测试连接", "连接成功");
				return;
			}else{
				AlertMaker.showSimpleAlert("测试连接", "连接失败");
				return;
			}
		} catch (Exception e) {
			log.error(e);
			AlertMaker.showErrorMessage("测试连接", "连接失败，原因：" + e.getMessage());
		}
	}

	@FXML
	private void handleSaveButtonAction(ActionEvent event) {
		sysConfig.setMailServerName(mailServerName.getText());
		sysConfig.setMailSmtpPort(Integer.parseInt(mailSmtpPort.getText()));
		sysConfig.setMailEmailAddress(mailEmailAddress.getText());
		sysConfig.setMailEmailPassword(mailEmailPassword.getText());
		sysConfig.setMailSslCheckbox(mailSslCheckbox.isSelected());
		DatabaseHandler.getInstance().updateSysConfig();
		AlertMaker.showSimpleAlert("保存", "基础配置修改成功");
	}

	@FXML
	private void handleSaverRunningAction(ActionEvent event) {
		sysConfig.setTaskFilePath(taskFilePath.getText());
		sysConfig.setLogPath(logPath.getText());
		sysConfig.setControlServer(controlServer.getText());
		DatabaseHandler.getInstance().updateSysConfig();
		AlertMaker.showSimpleAlert("保存", "任务配置修改成功");
	}

	@FXML
	private void refreshRunningAction(ActionEvent event){
		initData();
	}

	@FXML
	private void handleSaveAlertAction(ActionEvent event) {
		sysConfig.setMiniteErrorLimit(Integer.parseInt(miniteErrorLimit.getText()));
		sysConfig.setRunningLimit(Integer.parseInt(runningLimit.getText()));
		DatabaseHandler.getInstance().updateSysConfig();
		AlertMaker.showSimpleAlert("保存", "预警配置修改成功");
	}


	@FXML
	private void handleDatabaseExportAction(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select Location to Create Backup");
		File selectedDirectory = directoryChooser.showDialog(getStage());
		if (selectedDirectory == null) {
			AlertMaker.showErrorMessage("Export cancelled", "No Valid Directory Found");
		} else {
			DatabaseExporter databaseExporter = new DatabaseExporter(selectedDirectory);
			progressSpinner.visibleProperty().bind(databaseExporter.runningProperty());
			new Thread(databaseExporter).start();
		}
	}

	private void initData() {
		mailServerName.setText(sysConfig.getMailServerName());
		mailSmtpPort.setText(sysConfig.getMailSmtpPort().toString());
		mailEmailAddress.setText(sysConfig.getMailEmailAddress());
		mailEmailPassword.setText(sysConfig.getMailEmailPassword());
		mailSslCheckbox.setSelected(sysConfig.getMailSslCheckbox());

		taskFilePath.setText(sysConfig.getTaskFilePath());
		logPath.setText(sysConfig.getLogPath());
		controlServer.setText(sysConfig.getControlServer());
		if (sysConfig.getConnectTime() != null) {
			connectStatus.setText("已连接");
			connectDuration.setText(DateUtil.formatBetween(sysConfig.getConnectTime(),
					new Date(),
					BetweenFormater.Level.MINUTE));
		} else {
			connectStatus.setText("未连接");
			connectDuration.setText("");
		}
		dbPath.setText(sysConfig.getDbPath());
		dbPath.setEditable(false);

		miniteErrorLimit.setText(sysConfig.getMiniteErrorLimit().toString());
		runningLimit.setText(sysConfig.getRunningLimit().toString());
	}

	private Stage getStage() {
		return (Stage) taskFilePath.getScene().getWindow();
	}
}
