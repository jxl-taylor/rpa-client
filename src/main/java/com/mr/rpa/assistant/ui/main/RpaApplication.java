package com.mr.rpa.assistant.ui.main;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.license.LicenseManagerHolder;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.util.AssistantUtil;
import lombok.extern.log4j.Log4j;
import org.quartz.SchedulerException;

@Log4j
public class RpaApplication extends Application {

	private SimpleStringProperty titleProperty = new SimpleStringProperty();

	@Override
	public void start(Stage stage) throws Exception {
		log.info("MR-BOT Client Starting...");
		DatabaseHandler.getInstance();
		Parent root = FXMLLoader.load(getClass().getResource("/assistant/ui/login/login.fxml"));

		Scene scene = new Scene(root);

		AssistantUtil.setStageIcon(stage);
		if(!LicenseManagerHolder.getLicenseManagerHolder().verifyInstall() || !LicenseManagerHolder.getLicenseManagerHolder().verifyCert()){
			AlertMaker.showMaterialDialog(((StackPane) root),
					((StackPane) root).getChildren().get(0),
					GlobalProperty.getInstance().getExitBtns().subList(0,1), "License", "License不可用", false);
		}

		stage.setFullScreen(false);
		stage.setResizable(false);
 		stage.setScene(scene);
		stage.show();
		stage.setTitle("MR-BOT");

	}

	public static void main(String[] args) {
		Long startTime = System.currentTimeMillis();
		launch(args);
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

}
