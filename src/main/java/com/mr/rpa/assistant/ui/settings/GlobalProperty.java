package com.mr.rpa.assistant.ui.settings;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.event.AfterLoginEventHandler;
import com.mr.rpa.assistant.ui.main.MainController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Data;

import java.util.List;

/**
 * Created by feng on 2020/2/4 0004
 */
@Data
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

	//右角菜单显示控制

	private SimpleBooleanProperty taskPaneVisible = new SimpleBooleanProperty(true);

	private SimpleBooleanProperty settingPaneVisible = new SimpleBooleanProperty(false);

	private SimpleBooleanProperty myInfoPaneVisible = new SimpleBooleanProperty(false);

	private SimpleBooleanProperty taskHistoryPaneVisible = new SimpleBooleanProperty(false);

	private SimpleBooleanProperty taskLogPaneVisible = new SimpleBooleanProperty(false);

	private MainController mainController;

	private AfterLoginEventHandler afterLoginEventHandler;

	private StackPane rootPane;

	private List<JFXButton> exitBtns;

	//selectd Task
	private SimpleStringProperty selectedTaskId = new SimpleStringProperty();

	private SysConfig sysConfig = new SysConfig();

	public void setAfterLoginEventHandler(VBox toolbar) {
		this.afterLoginEventHandler = new AfterLoginEventHandler(toolbar);
	}

	public void setTitle(String username) {
		this.title.set(String.format("MR-ROBOT（用户：%s）", username));
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

	public void doAfterLogin(){
		mainController.doAfterLogin();
	}
}
