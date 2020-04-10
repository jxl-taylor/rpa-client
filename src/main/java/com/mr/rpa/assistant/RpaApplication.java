package com.mr.rpa.assistant;

import com.alibaba.fastjson.JSON;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.HeartBeat;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.util.AssistantUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j
public class RpaApplication extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		log.info("REC Client Starting...");
		Parent root = FXMLLoader.load(getClass().getResource("/assistant/ui/login/login.fxml"));
		Scene scene = new Scene(root);

		AssistantUtil.setStageIcon(stage);

		if (!LicenseManagerHolder.getLicenseManagerHolder().verifyInstall() || !LicenseManagerHolder.getLicenseManagerHolder().verifyCert()) {
			//如果验证不通过，尝试从控制中心下载license 和公钥（如果第一次登录，直接下载30天的试用版license）
			AtomicBoolean retryVerifing = new AtomicBoolean(false);
			String controlUrl = GlobalProperty.getInstance().getSysConfig().getControlServer();
			if (StringUtils.isNotBlank(controlUrl)) {
				try {
					CommonUtil.requestControlCenter(controlUrl,
							SystemContants.API_SERVICE_ID_QUERY_LIC_DOWNLOAD_URL,
							JSON.toJSONString(new HashMap<String, String>()),
							resultJson -> {
								retryVerifing.set(new HeartBeat().downLoadAndInstallLic(resultJson.getString("licDownloadUrl")));
							});
				} catch (Throwable e) {
					log.error(e);
					AlertMaker.showErrorMessage("用户查询", e.getMessage());
				}
			}
			if (!retryVerifing.get()) {
				AlertMaker.showMaterialDialog(((StackPane) root),
						((StackPane) root).getChildren().get(0),
						GlobalProperty.getInstance().getExitBtns().subList(0, 1), "License", "License不可用", false);

			}
		}

		stage.setFullScreen(false);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.setTitle("REC");

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
