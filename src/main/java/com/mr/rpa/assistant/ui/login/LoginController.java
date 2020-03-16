package com.mr.rpa.assistant.ui.login;

import cn.hutool.core.io.FileUtil;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

@Log4j
public class LoginController implements Initializable {

	private static final String RPA_CONTROL_CENTER = "http://microrule.com/";
	private static final String CACHE_FILE = System.getProperty("user.dir") + File.separator + ".cache";

	@FXML
	private AnchorPane loginPane;

	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;
	@FXML
	private Hyperlink applyLink;
	@FXML
	private JFXCheckBox saveChx;

	private SysConfig sysConfig;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		sysConfig = GlobalProperty.getInstance().getSysConfig();
		username.setText(sysConfig.getAdminUsername());
		try {
			if (FileUtil.exist(CACHE_FILE)) {
				String cacheContent = FileUtil.readUtf8String(CACHE_FILE);
				String[] arr = cacheContent.split("=");
				username.setText(arr[0]);
				password.setText(arr[1]);
			}
		} catch (Throwable e) {
			log.error(e);
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				password.requestFocus();
			}
		});

	}

	@FXML
	private void handleLoginButtonAction(ActionEvent event) {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		String uname = StringUtils.trimToEmpty(username.getText());
		String pword = password.getText();
		User user = sysConfig.getAdminUser();
		if (uname.equals(user.getUsername()) && pword.equals(user.getPassword())) {
			closeStage();
			setUserInfo();
			loadMain();
			if (saveChx.isSelected()) {
				cachePwd();
			} else {
				FileUtil.del(CACHE_FILE);
			}
			globalProperty.setCurrentUser(user);
			globalProperty.getMyInfoController().initCurrentUser();
			log.info(String.format("User successfully logged in %s", uname));
		} else {
			username.getStyleClass().add("wrong-credentials");
			password.getStyleClass().add("wrong-credentials");
		}
	}

	private void cachePwd() {
		FileUtil.del(CACHE_FILE);
		FileUtil.writeUtf8String(username.getText() + "=" + password.getText(), new File(CACHE_FILE));
	}

	private void setUserInfo() {
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		globalProperty.setTitle(username.getText());
	}

	@FXML
	private void handleCancelButtonAction(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	private void linkToApply(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URI(RPA_CONTROL_CENTER));
			applyLink.setVisited(false);
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void closeStage() {
		((Stage) username.getScene().getWindow()).close();
	}

	private void loadMain() {
		try {
			Parent parent = FXMLLoader.load(getClass().getResource("/assistant/ui/main/main.fxml"));
			Stage stage = new Stage(StageStyle.DECORATED);
			stage.setScene(new Scene(parent));
			stage.show();
			stage.titleProperty().bind(GlobalProperty.getInstance().getTitle());

			AssistantUtil.setStageIcon(stage);
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					AlertMaker.showMaterialDialog(((StackPane) parent),
							((StackPane) parent).getChildren().get(0),
							GlobalProperty.getInstance().getExitBtns(), "退出", "", false);
					event.consume();
				}
			});

			stage.setFullScreen(false);
			stage.setResizable(false);

			//启动定时任务
			JobFactory.start();
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                stage.setIconified(true);
//            }
//        });
//        new Thread(()->{
//            for(;;){
//                LOGGER.info("MR-RPA TEST LOG................................");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();

//			AlertMaker.showMaterialDialog(((StackPane) parent),
//					((StackPane) parent).getChildren().get(0),
//					GlobalProperty.getInstance().getExitBtns().subList(0,1), "License", "License无效", true);
			GlobalProperty.getInstance().setStartDate(new java.util.Date());
		} catch (Exception ex) {
			log.error(ex);
			AlertMaker.showErrorMessage("初始化环境", String.format("初始化环境失败， 原因：%s", ex.getMessage()));
		}
	}
}
