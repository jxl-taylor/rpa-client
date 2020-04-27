package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.mail.MailUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jfoenix.controls.*;
import com.mr.rpa.assistant.data.callback.GenericCallback;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.data.model.SysConfig;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.mr.rpa.assistant.job.HeartBeat;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.util.SystemContants;
import com.mr.rpa.assistant.util.email.EmailUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.database.export.DatabaseExporter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

@Log4j
public class SettingsController implements Initializable {

	@FXML
	private JFXTextField mailServerName;
	@FXML
	private JFXTextField mailSmtpPort;
	@FXML
	private JFXTextField mailEmailAddress;
	@FXML
	private JFXPasswordField mailEmailPassword;
	@FXML
	private JFXCheckBox mailSslCheckbox;
	@FXML
	private VBox toMailBox;
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
//	@FXML
//	private JFXTextField miniteErrorLimit;
//	@FXML
//	private JFXTextField runningLimit;
	@FXML
	private JFXSpinner progressSpinner;
	@FXML
	private StackPane rootPane;
	@FXML
	private JFXTabPane settingTabPane;
	@FXML
	private Tab mailTab;
	private boolean mailsaved;

	private LinkedHashMap<Object, HBox> toMailMap = Maps.newLinkedHashMap();

	GlobalProperty globalProperty = GlobalProperty.getInstance();

	private SysConfig sysConfig = globalProperty.getSysConfig();

	private SysConfigService sysConfigService = ServiceFactory.getService(SysConfigService.class);

	private MailServerInfo defaultMailServerInfo = GlobalProperty.getInstance().getDefaultMailServerInfo();

	private JFXButton cancelBtn;

	private GenericCallback mailCallBack;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initData();
		initComponents();
		cancelBtn = new JFXButton("取消");
		cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			rootPane.getChildren().get(0).setEffect(null);
		});
	}

	private void initComponents() {
		settingTabPane.tabMinWidthProperty().bind(settingTabPane.widthProperty().divide(settingTabPane.getTabs().size()).subtract(5));
		JFXButton confirmBtn = new JFXButton("确定");
		confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				handleSaveMailAction(null);
			} catch (Exception e) {
				log.error(e);
				AlertMaker.showErrorMessage(e);
				return;
			}
		});
		mailTab.setOnSelectionChanged((Event event) -> {
			if (mailTab.isSelected()) {
				initMailSetting();
			} else {
				if (!mailsaved)
					AlertMaker.showMaterialDialog(rootPane,
							rootPane.getChildren().get(0),
							Lists.newArrayList(confirmBtn, cancelBtn), "通知配置",
							"保存配置么?", false);
			}
		});
	}

	@FXML
	private void testConnection(ActionEvent event) {
		HeartBeat heartBeat = new HeartBeat();
		try {
			if (StringUtils.isBlank(controlServer.getText())) {
				AlertMaker.showErrorMessage("测试连接", "控制中心地址为空，请输入地址");
				return;
			}
			if (heartBeat.action(controlServer.getText())) {
				AlertMaker.showSimpleAlert("测试连接", "连接成功");
				return;
			} else {
				AlertMaker.showSimpleAlert("测试连接", "连接失败");
				return;
			}
		} catch (Exception e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
		}
	}

	@FXML
	private void handleSaveMailAction(ActionEvent event) {
		String checkMsg = checkMailSave();
		if (StringUtils.isNotBlank(checkMsg)) {
			AlertMaker.showErrorMessage("保存", checkMsg);
			return;
		}
		mailsaved = true;
		sysConfig.setMailServerName(mailServerName.getText());
		sysConfig.setMailSmtpPort(Integer.parseInt(mailSmtpPort.getText()));
		sysConfig.setMailEmailAddress(mailEmailAddress.getText());
		sysConfig.setMailEmailPassword(mailEmailPassword.getText());
		sysConfig.setMailSslCheckbox(mailSslCheckbox.isSelected());
		Set<String> mailSet = Sets.newLinkedHashSet();
		toMailMap.values().forEach(hBox -> {
			JFXTextField mailField = (JFXTextField) hBox.getChildren().get(0);
			mailSet.add(mailField.getText());
		});
		sysConfig.setToMails(StringUtils.join(mailSet, ","));
		sysConfigService.update();
		AlertMaker.showSimpleAlert("保存", "通知管理配置修改成功");
	}

	private String checkMailSave() {
		//邮件格式正则表达式
		if (StringUtils.isBlank(mailServerName.getText())) return "SMTP主机不能为空";
		if (StringUtils.isBlank(mailEmailAddress.getText())) return "管理员Email账号不能为空";
//		if(!mailEmailAddress.getText().matches(mailRegex)) return "管理员Email账号格式不正确";
		if (StringUtils.isBlank(mailEmailPassword.getText())) return "管理员Email密码不能为空";
		if (StringUtils.isBlank(mailSmtpPort.getText())) return "SMTP端口号不能为空";
		for (HBox hBox : toMailMap.values()) {
			JFXTextField mailField = (JFXTextField) hBox.getChildren().get(0);
			if (StringUtils.isBlank(mailField.getText())) return "收件人账号不能为空";
			if (!mailField.getText().matches(SystemContants.MAIL_REGEX))
				return String.format("收件人[%s]账号格式不正确", mailField.getText());
		}
		return null;
	}

	@FXML
	private void setDefaultAdminEmailAction(ActionEvent event) {
		mailServerName.setText(defaultMailServerInfo.getMailServer());
		mailEmailAddress.setText(defaultMailServerInfo.getEmailID());
		mailEmailPassword.setText(defaultMailServerInfo.getPassword());
		mailSmtpPort.setText(String.valueOf(defaultMailServerInfo.getPort()));
		mailSslCheckbox.setSelected(defaultMailServerInfo.getSslEnabled());
	}

	@FXML
	private void doAddToMail(String toMail) {
		HBox paramBox = new HBox();
		paramBox.setStyle("-fx-border-color: #2A2E37; -fx-background-color: #2A2E37");
		paramBox.setSpacing(5);

		JFXTextField mailField = new JFXTextField();
		mailField.setLabelFloat(false);
		mailField.setPromptText("收件人");
		mailField.setPrefWidth(330);
		if (StringUtils.isNotBlank(toMail)) mailField.setText(toMail);
		paramBox.getChildren().add(mailField);
		HBox.setHgrow(mailField, Priority.ALWAYS);

		//删除事件
		JFXButton deleteButton = new JFXButton("删除");
		deleteButton.setPrefWidth(100);
		deleteButton.setOnAction((final ActionEvent e) -> {
			HBox selectedHBox = toMailMap.get(e.getSource());
			toMailBox.getChildren().remove(selectedHBox);
			toMailMap.remove(e.getSource());
		});
		paramBox.getChildren().add(deleteButton);
		toMailMap.put(deleteButton, paramBox);
		toMailBox.getChildren().add(paramBox);
	}

	@FXML
	private void addToMailAction(ActionEvent event) {
		doAddToMail(null);
	}

	@FXML
	private void testMailAction(ActionEvent event) {
		String checkMsg = checkMailSave();
		if (StringUtils.isNotBlank(checkMsg)) {
			AlertMaker.showSimpleAlert("邮件发送", checkMsg);
			return;
		}
		if (toMailMap.size() == 0) {
			AlertMaker.showSimpleAlert("邮件发送", "收件人不能为空!");
			return;
		}
		MailServerInfo mailServerInfo = new MailServerInfo(mailServerName.getText(),
				Integer.parseInt(mailSmtpPort.getText()),
				mailEmailAddress.getText(), mailEmailPassword.getText(), mailSslCheckbox.isSelected());
		EmailUtil.sendTestMail(mailServerInfo, StringUtils.join(toMailMap.values().stream()
				.map(item -> ((JFXTextField) item.getChildren().get(0)).getText())
				.collect(Collectors.toSet()), ","));
	}

	@FXML
	private void handleSaverRunningAction(ActionEvent event) {
		sysConfig.setTaskFilePath(taskFilePath.getText());
		sysConfig.setLogPath(logPath.getText());
		sysConfig.setControlServer(controlServer.getText());
		sysConfigService.update();
		AlertMaker.showSimpleAlert("保存", "任务配置修改成功");
	}

	@FXML
	private void refreshRunningAction(ActionEvent event) {
		initData();
	}

//	@FXML
//	private void handleSaveAlertAction(ActionEvent event) {
//		sysConfig.setMiniteErrorLimit(Integer.parseInt(miniteErrorLimit.getText()));
//		sysConfig.setRunningLimit(Integer.parseInt(runningLimit.getText()));
//		sysConfigService.update();
//		AlertMaker.showSimpleAlert("保存", "预警配置修改成功");
//	}


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
		initMailSetting();
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

//		miniteErrorLimit.setText(sysConfig.getMiniteErrorLimit().toString());
//		runningLimit.setText(sysConfig.getRunningLimit().toString());
	}

	private void initMailSetting() {
		mailsaved = false;
		toMailBox.getChildren().clear();
		toMailMap.clear();
		mailServerName.setText(sysConfig.getMailServerName());
		mailSmtpPort.setText(sysConfig.getMailSmtpPort().toString());
		mailEmailAddress.setText(sysConfig.getMailEmailAddress());
		mailEmailPassword.setText(sysConfig.getMailEmailPassword());
		mailSslCheckbox.setSelected(sysConfig.getMailSslCheckbox());
		if (StringUtils.isNotBlank(sysConfig.getToMails())) {
			String[] toMailArray = StringUtils.split(sysConfig.getToMails(), ",");
			for (String mail : toMailArray) {
				doAddToMail(mail);
			}
		}
	}

	private Stage getStage() {
		return (Stage) taskFilePath.getScene().getWindow();
	}
}
