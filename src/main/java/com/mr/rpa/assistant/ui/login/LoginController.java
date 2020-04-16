package com.mr.rpa.assistant.ui.login;

import cn.hutool.core.io.FileUtil;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.service.UserService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.Pair;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

@Log4j
public class LoginController implements Initializable {

	private static final String CACHE_FILE = System.getProperty("user.dir") + File.separator + ".cache";

	@FXML
	private StackPane rootPane;

	@FXML
	private JFXTextField username;
	@FXML
	private JFXPasswordField password;
	@FXML
	private Hyperlink applyLink;
	@FXML
	private JFXCheckBox saveChx;

	private SysConfig sysConfig;

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	private UserService userService = ServiceFactory.getService(UserService.class);

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
			AlertMaker.showErrorMessage(e);
			return;
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				password.requestFocus();
			}
		});

	}

	public StackPane getRootPane() {
		return rootPane;
	}

	private User checkLogin() {
		String uname = username.getText();
		if (StringUtils.isBlank(uname)) {
			AlertMaker.showSimpleAlert("登录", "用户名不能为空");
			return null;
		}
		String pword = password.getText();
		User user = sysConfig.getAdminUser();
		if (uname.equals(user.getUsername())) {
			if (user.getPassword().equals(pword)) return user;
			AlertMaker.showSimpleAlert("登录", "密码错误");
		} else {
			List<User> users = userService.getUserListByUsername(uname);
			if (users.size() == 0) {
				AlertMaker.showSimpleAlert("登录", "用户名不存在");
			} else {
				if (users.get(0).getPassword().equals(pword)) return users.get(0);
				AlertMaker.showSimpleAlert("登录", "密码错误");
			}
		}
		return null;
	}

	@FXML
	private void handleLoginButtonAction(ActionEvent event) {
		User user = checkLogin();
		if (user != null) {
			if( globalProperty.getLoginController() == null){
				if (saveChx.isSelected()) {
					cachePwd();
				} else {
					FileUtil.del(CACHE_FILE);
				}
				globalProperty.setLoginController(this);
				GlobalProperty.getInstance().setStartDate(new java.util.Date());
			}
			closeStage();
			loadMain();
			globalProperty.getMyInfoController().initCurrentUser(user);
			globalProperty.getRootPane().getChildren().get(0).setEffect(null);
			globalProperty.getRootPane().getChildren().get(0).setDisable(false);

			log.info(String.format("User successfully logged in %s", user.getUsername()));
		} else {
			username.getStyleClass().add("wrong-credentials");
			password.getStyleClass().add("wrong-credentials");
		}
	}

	private void cachePwd() {
		FileUtil.del(CACHE_FILE);
		FileUtil.writeUtf8String(username.getText() + "=" + password.getText(), new File(CACHE_FILE));
	}

	@FXML
	private void handleCancelButtonAction(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	private void linkToApply(ActionEvent event) {
//		try {
//			Desktop.getDesktop().browse(new URI(RPA_CONTROL_CENTER));
//			applyLink.setVisited(false);
//		} catch (IOException | URISyntaxException e1) {
//			e1.printStackTrace();
//		}
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/myinfo/register.fxml"), "注册", null);
	}

	private void closeStage() {
		username.clear();
		password.clear();
		((Stage) rootPane.getScene().getWindow()).close();
	}

	private void loadMain() {
		try {
			Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/main/main.fxml"),
					GlobalProperty.getInstance().getTitle().get(), null);
			Stage stage = pair.getObject1();
			stage.titleProperty().bind(GlobalProperty.getInstance().getTitle());
			rootPane.getChildren().get(0).setEffect(null);
			rootPane.getChildren().get(0).setDisable(false);
			stage.setOnCloseRequest(e -> {
				AlertMaker.showMaterialDialog(globalProperty.getRootPane(),
						globalProperty.getRootPane().getChildren().get(0),
						GlobalProperty.getInstance().getExitBtns(), "注销/退出", "", false, false);
				e.consume();
			});

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
		} catch (Exception ex) {
			log.error(ex);
			AlertMaker.showErrorMessage(ex);
		}
	}
}
