package com.mr.rpa.assistant.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.mr.rpa.assistant.database.DatabaseHandler;
import org.apache.log4j.Logger;

public class MainController implements Initializable {

	private final static Logger LOGGER = Logger.getLogger(MainController.class);

	private DatabaseHandler databaseHandler = DatabaseHandler.getInstance();

	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane taskPane;
	@FXML
	private AnchorPane settingPane;
	@FXML
	private AnchorPane myInfoPane;
	@FXML
	private AnchorPane taskHistoryPane;

	@FXML
	private AnchorPane taskLogPane;

	@FXML
	private HBox topMenu;
	@FXML
	private JFXButton taskShowButton;

	@FXML
	private JFXButton settingShowButton;

	@FXML
	private JFXButton myInfoShowButton;

	@FXML
	private JFXButton loginShowButton;

	@FXML
	private JFXHamburger hamburger;
	@FXML
	private JFXDrawer drawer;
	@FXML
	private AnchorPane rootAnchorPane;
	@FXML
	private HBox taskDataContainer;

	@FXML
	private Tab statisticTab;
	@FXML
	private Tab taskTab;
	@FXML
	private JFXTabPane mainTabPane;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setRootPane(rootPane);
		globalProperty.setMainController(this);

		initShowButtonAction();
		initDrawer();
		initStatisticTab();
		initComponents();

		AlertMaker.showTrayMessage(String.format("您好 %s!", System.getProperty("user.name")), "感谢使用迈荣机器人");
	}

	private void initShowButtonAction() {
		topMenu.getChildren().remove(myInfoShowButton);
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		taskPane.visibleProperty().bind(globalProperty.getTaskPaneVisible());
		settingPane.visibleProperty().bind(globalProperty.getSettingPaneVisible());
		myInfoPane.visibleProperty().bind(globalProperty.getMyInfoPaneVisible());
		taskHistoryPane.visibleProperty().bind(globalProperty.getTaskHistoryPaneVisible());
		taskLogPane.visibleProperty().bind(globalProperty.getTaskLogPaneVisible());

		loginShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			LibraryAssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/login/login.fxml"), "登录", null);

		});
		taskShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskPaneVisible().setValue(true);
			globalProperty.getSettingPaneVisible().setValue(false);
			globalProperty.getMyInfoPaneVisible().setValue(false);
			recoverLogPane();
		});
		settingShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskPaneVisible().setValue(false);
			globalProperty.getSettingPaneVisible().setValue(true);
			globalProperty.getMyInfoPaneVisible().setValue(false);
			recoverLogPane();
		});
		myInfoShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskPaneVisible().setValue(false);
			globalProperty.getSettingPaneVisible().setValue(false);
			globalProperty.getMyInfoPaneVisible().setValue(true);
			recoverLogPane();

		});
	}

	private void recoverLogPane() {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.getTaskHistoryPaneVisible().setValue(false);
		globalProperty.getTaskLogPaneVisible().setValue(false);
		globalProperty.getLogAreaMinHeight().set(globalProperty.DEFAULT_LOG_HEIGHT);
		globalProperty.getLogListHeight().set(globalProperty.DEFAULT_LOG_LIST_HEIGHT);
		globalProperty.getTaskBeanController().refreshSplit();
	}



	private void initDrawer() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("assistant/ui/main/toolbar/toolbar.fxml"));
			VBox toolbar = loader.load();
			drawer.setSidePane(toolbar);
			GlobalProperty.getInstance().setAfterLoginEventHandler(toolbar);
		} catch (IOException ex) {
			LOGGER.error(ex);
		}
		HamburgerSlideCloseTransition task = new HamburgerSlideCloseTransition(hamburger);
		task.setRate(-1);
		hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			drawer.toggle();
		});
		drawer.setOnDrawerOpening((event) -> {
			task.setRate(task.getRate() * -1);
			task.play();
			drawer.toFront();
		});
		drawer.setOnDrawerClosed((event) -> {
			drawer.toBack();
			task.setRate(task.getRate() * -1);
			task.play();
		});
	}

	private void initStatisticTab() {
		statisticTab.setOnSelectionChanged((Event event) -> {
			if (statisticTab.isSelected()) {
				GlobalProperty.getInstance().getStatisticController().refreshGraphs();
			}
		});
	}

	private void initComponents() {
		mainTabPane.tabMinWidthProperty().bind(rootAnchorPane.widthProperty().divide(mainTabPane.getTabs().size()).subtract(15));

	}

	public void doAfterLogin() {
		topMenu.getChildren().remove(loginShowButton);
		topMenu.getChildren().add(myInfoShowButton);
	}

}
