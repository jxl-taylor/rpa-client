package com.mr.rpa.assistant;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.util.AssistantUtil;
import lombok.extern.log4j.Log4j;
import org.quartz.SchedulerException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Log4j
public class RpaApplication extends Application {

	private AbstractApplicationContext applicationContext;

	public static void main(final String[] args) {
		Long startTime = System.currentTimeMillis();
		Application.launch(args);
		Long endTime = System.currentTimeMillis();
		log.info(String.format("MR-BOT Client is started successfully on %s. Used for %s ms", AssistantUtil.formatDateTimeString(startTime), endTime));

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

	@Override
	public void init() throws Exception {
		log.info("MR-BOT Client Starting...");
		GlobalProperty.applicationContext = new ClassPathXmlApplicationContext("spring.xml");
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent rootNode  = CommonUtil.loadFXml(getClass().getResource("/assistant/ui/login/login.fxml"));
		stage.setScene(new Scene(rootNode));
		AssistantUtil.setStageIcon(stage);
		if (!LicenseManagerHolder.getLicenseManagerHolder().verifyInstall() || !LicenseManagerHolder.getLicenseManagerHolder().verifyCert()) {
			AlertMaker.showMaterialDialog(((StackPane) rootNode),
					((StackPane) rootNode).getChildren().get(0),
					GlobalProperty.getInstance().getExitBtns().subList(0, 1), "License", "License不可用", false);
		}

		stage.setFullScreen(false);
		stage.setResizable(false);
		stage.show();
		stage.setTitle("MR-BOT");
	}

}
