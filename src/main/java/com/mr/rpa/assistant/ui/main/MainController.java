package com.mr.rpa.assistant.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

public class MainController implements Initializable {

	private final static Logger LOGGER = Logger.getLogger(MainController.class);

	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane taskPane;
	@FXML
	private AnchorPane myInfoPane;
	@FXML
	private AnchorPane statisticPane;
	@FXML
	private AnchorPane taskHistoryPane;

	@FXML
	private AnchorPane taskLogPane;

	@FXML
	private HBox topMenu;
	@FXML
	private JFXButton taskShowButton;

	@FXML
	private JFXButton myInfoShowButton;

	@FXML
	private JFXButton statisticShowButton;

	@FXML
	private JFXHamburger hamburger;
	@FXML
	private JFXDrawer drawer;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		globalProperty.setRootPane(rootPane);
		globalProperty.setMainController(this);

		initShowButtonAction();
		initDrawer();

		if (System.getProperty("os.name").contains("Win")) {
			AlertMaker.showTrayMessage(String.format("您好 %s!", System.getProperty("user.name")), "感谢使用RPA机器人");
		}
	}

	/**
	 * 设置权限
	 */
	public void setRight() {
		topMenu.getChildren().clear();
		if (CommonUtil.isAdmin(globalProperty.getCurrentUser().getUsername())) {
			topMenu.getChildren().addAll(taskShowButton, statisticShowButton, myInfoShowButton);
		} else {
			topMenu.getChildren().addAll(taskShowButton, myInfoShowButton);

		}
	}

	private void initShowButtonAction() {
		taskPane.visibleProperty().bind(globalProperty.getTaskPaneVisible());
		myInfoPane.visibleProperty().bind(globalProperty.getMyInfoPaneVisible());
		statisticPane.visibleProperty().bind(globalProperty.getStatisticPaneVisible());
		taskHistoryPane.visibleProperty().bind(globalProperty.getTaskHistoryPaneVisible());
		taskLogPane.visibleProperty().bind(globalProperty.getTaskLogPaneVisible());

		taskShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			if (!globalProperty.getTaskPaneVisible().get()) globalProperty.getSelectedTaskLogId().set("");
			globalProperty.getTaskPaneVisible().setValue(true);
			globalProperty.getMyInfoPaneVisible().setValue(false);
			globalProperty.getStatisticPaneVisible().setValue(false);
			recoverPane();
		});
		myInfoShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskPaneVisible().setValue(false);
			globalProperty.getMyInfoPaneVisible().setValue(true);
			globalProperty.getStatisticPaneVisible().setValue(false);
			recoverPane();

		});
		statisticShowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event event) -> {
			globalProperty.getTaskPaneVisible().setValue(false);
			globalProperty.getMyInfoPaneVisible().setValue(false);
			globalProperty.getStatisticPaneVisible().setValue(true);
			globalProperty.getStatisticController().refreshGraphs();
			recoverPane();

		});
	}

	private void recoverPane() {
		globalProperty.getTaskHistoryPaneVisible().setValue(false);
		globalProperty.getTaskLogPaneVisible().setValue(false);
		globalProperty.getTaskBeanController().refreshSplit();
	}


	public void initDrawer() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("assistant/ui/main/toolbar/toolbar.fxml"));
			VBox toolbar = loader.load();
			drawer.setSidePane(toolbar);
			drawer.toBack();
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

}
