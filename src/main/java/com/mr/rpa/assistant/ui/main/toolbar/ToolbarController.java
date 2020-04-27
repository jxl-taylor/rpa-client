package com.mr.rpa.assistant.ui.main.toolbar;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.Pair;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

public class ToolbarController implements Initializable {

    private GlobalProperty globalProperty = GlobalProperty.getInstance();
    @FXML
	private VBox toolVBox;
	@FXML
	private JFXButton registerBtn;
	@FXML
	private JFXButton setBtn;
	@FXML
	private JFXButton updateBtn;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		globalProperty.setToolbarController(this);
	}

	public void setRight() {
		if(!CommonUtil.isAdmin(globalProperty.getCurrentUser().getUsername())){
			toolVBox.getChildren().removeAll(registerBtn, setBtn, updateBtn);
		}
	}

	@FXML
	private void loadAddTask(ActionEvent event) {
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);
	}

	@FXML
	private void loadRegister(ActionEvent event) {
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/myinfo/register.fxml"), "注册", null);
	}

	@FXML
	private void loadUpdate(ActionEvent event) {
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/about/version_update.fxml"), "更新", null);
		pair.getObject1().setOnCloseRequest((e) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/about/version_update.fxml"));
		});
	}

	@FXML
	private void loadBotMarket(ActionEvent event) {
		SysConfig sysConfig = globalProperty.getSysConfig();
		if (StringUtils.isEmpty(sysConfig.getControlServer())) {
			AlertMaker.showErrorMessage("BOT市场", "控制中心未配置");
			return;
		}
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/botstore/bot_market_query.fxml"), "BOT市场", null);
		pair.getObject1().setOnCloseRequest((e) -> {
			AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/botstore/bot_market_query.fxml"));
		});
	}

	@FXML
	private void exit(ActionEvent event) {
		AlertMaker.showMaterialDialog(globalProperty.getRootPane(),
				globalProperty.getRootPane().getChildren().get(0),
				GlobalProperty.getInstance().getExitBtns(globalProperty.getCurrentUser().getUsername()),
				"注销/退出", "", false, false);
	}

	@FXML
	private void LoadAboutUs(ActionEvent event) {
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/about/about.fxml"), "关于我们", null);
	}

	@FXML
	private void loadSettings(ActionEvent event) {
		AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/settings/settings.fxml"), "设置", null);
	}

}
