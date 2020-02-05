package com.mr.rpa.assistant.ui.settings;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.event.AfterLoginEventHandler;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Created by feng on 2020/2/4 0004
 */
public class GlobalProperty {

	private static GlobalProperty globalProperty = new GlobalProperty();

	private GlobalProperty() {
	}

	public static GlobalProperty getInstance() {
		return globalProperty;
	}

	public static String TITLE_PREFIX = "MR-ROBOT";

	private SimpleStringProperty title = new SimpleStringProperty("MR-ROBOT（未登录）");

	private SimpleStringProperty stmpServerName = new SimpleStringProperty();
	private SimpleStringProperty stmpPort = new SimpleStringProperty();
	private SimpleStringProperty emailUsername = new SimpleStringProperty();
	private SimpleStringProperty emailPassword = new SimpleStringProperty();
	private SimpleBooleanProperty sslEnabled = new SimpleBooleanProperty();

	private AfterLoginEventHandler afterLoginEventHandler;

	private StackPane rootPane;
	private List<JFXButton> exitBtns;

	private SysConfig sysConfig = new SysConfig();

	public String getTitle() {
		return title.get();
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public void setTitle(String username) {
		this.title.set(String.format("MR-ROBOT（用户：%s）", username));
	}

	public String getStmpServerName() {
		return stmpServerName.get();
	}

	public SimpleStringProperty stmpServerNameProperty() {
		return stmpServerName;
	}

	public void setStmpServerName(String stmpServerName) {
		this.stmpServerName.set(stmpServerName);
	}

	public String getStmpPort() {
		return stmpPort.get();
	}

	public SimpleStringProperty stmpPortProperty() {
		return stmpPort;
	}

	public void setStmpPort(String stmpPort) {
		this.stmpPort.set(stmpPort);
	}

	public String getEmailUsername() {
		return emailUsername.get();
	}

	public SimpleStringProperty emailUsernameProperty() {
		return emailUsername;
	}

	public void setEmailUsername(String emailUsername) {
		this.emailUsername.set(emailUsername);
	}

	public String getEmailPassword() {
		return emailPassword.get();
	}

	public SimpleStringProperty emailPasswordProperty() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword.set(emailPassword);
	}

	public boolean isSslEnabled() {
		return sslEnabled.get();
	}

	public SimpleBooleanProperty sslEnabledProperty() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled.set(sslEnabled);
	}

	public AfterLoginEventHandler getAfterLoginEventHandler() {
		return afterLoginEventHandler;
	}

	public void setAfterLoginEventHandler(VBox toolbar) {
		this.afterLoginEventHandler = new AfterLoginEventHandler(toolbar);
	}

	public StackPane getRootPane() {
		return rootPane;
	}

	public void setRootPane(StackPane rootPane) {
		this.rootPane = rootPane;
	}

	public List<JFXButton> getExitBtns() {

		if (exitBtns == null) {
			JFXButton confirmBtn = new JFXButton("确定");
			confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				System.exit(0);
			});

			JFXButton cancelBtn = new JFXButton("取消");
			cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				StackPane rootPane = getRootPane();
				rootPane.getChildren().get(0).setEffect(null);
			});
			exitBtns = Lists.newArrayList(confirmBtn, cancelBtn);
		}
		return exitBtns;
	}

	public SysConfig getSysConfig() {
		return sysConfig;
	}
}
