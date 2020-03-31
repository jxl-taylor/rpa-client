package com.mr.rpa.assistant;

import cn.hutool.core.io.FileUtil;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.HeartBeat;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
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

import java.io.File;

@Log4j
public class RpaApplication extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		log.info("REC Client Starting...");
		Parent root = FXMLLoader.load(getClass().getResource("/assistant/ui/login/login.fxml"));

		Scene scene = new Scene(root);

		AssistantUtil.setStageIcon(stage);

		//如果第一次登录，mrbot.lic 不存在，从控制中心下载试用版公钥文件和license
		if(!FileUtil.exist(System.getProperty("user.dir") + File.separator + SystemContants.LICENSE_PROPERTY_LIC_NAME)
				||!FileUtil.exist(System.getProperty("user.dir") + File.separator + SystemContants.LICENSE_PROPERTY_PUB_PATH)){
			if(StringUtils.isNotBlank(GlobalProperty.getInstance().getSysConfig().getControlCenterServer())){
				new HeartBeat().downLoadAndInstallLic();
			}
		}

		if(!LicenseManagerHolder.getLicenseManagerHolder().verifyInstall() || !LicenseManagerHolder.getLicenseManagerHolder().verifyCert()){
			AlertMaker.showMaterialDialog(((StackPane) root),
					((StackPane) root).getChildren().get(0),
					GlobalProperty.getInstance().getExitBtns().subList(0,1), "License", "License不可用", false);
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
