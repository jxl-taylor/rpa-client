package com.mr.rpa.assistant;

import com.alibaba.fastjson.JSON;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.HeartBeat;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.login.LoginController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.Pair;
import com.mr.rpa.assistant.util.SystemContants;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import javafx.application.Application;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.util.AssistantUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import java.io.File;
import java.util.HashMap;

@Log4j
public class RpaApplication extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		log.info("REC Client Starting...");
		Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/login/login.fxml"),
				"登录", null);
		StackPane rootPane = ((LoginController) pair.getObject2()).getRootPane();
		if (!LicenseManagerHolder.getLicenseManagerHolder().verifyInstall() || !LicenseManagerHolder.getLicenseManagerHolder().verifyCert()) {
			//如果验证不通过，尝试从控制中心下载license 和公钥（如果第一次登录，直接下载30天的试用版license）
			Boolean retryVerifing = false;
			String controlUrl = GlobalProperty.getInstance().getSysConfig().getControlServer();
			if (StringUtils.isNotBlank(controlUrl)) {
				try {
					HeartBeat heartBeat = new HeartBeat();
					heartBeat.action(null);
					retryVerifing = LicenseManagerHolder.getLicenseManagerHolder().verifyInstall()
							&& LicenseManagerHolder.getLicenseManagerHolder().verifyCert();
				} catch (Throwable e) {
					log.error(e);
					AlertMaker.showErrorMessage(e);
				}
			}
			if (retryVerifing != null && !retryVerifing) {
				AlertMaker.showMaterialDialog(rootPane,
						rootPane.getChildren().get(0),
						GlobalProperty.getInstance().getExitBtns(null).subList(0, 1), "License", "License不可用", false);

			}
		}

	}

	public static void main(String[] args) {
		Long startTime = System.currentTimeMillis();
		GlobalProperty globalProperty = GlobalProperty.getInstance();
		if (args.length > 0 && args[0].equalsIgnoreCase("debug")) globalProperty.setDebug(true);
		globalProperty.initDB();
		launch(args);
		Long endTime = System.currentTimeMillis();
		log.info(String.format("REC Client is started successfully on %s. Used for %s ms", AssistantUtil.formatDateTimeString(startTime), endTime));

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					JobFactory.exit();
				} catch (SchedulerException e) {
					log.error(e);
				}
				log.info(String.format("MR-RPA Client is closed on %s.", AssistantUtil.formatDateTimeString(System.currentTimeMillis())));

			}
		});
	}

}
