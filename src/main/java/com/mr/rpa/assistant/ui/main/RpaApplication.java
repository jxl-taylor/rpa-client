package com.mr.rpa.assistant.ui.main;

import com.mr.rpa.assistant.job.JobFactory;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

public class RpaApplication extends Application {

	private final static Logger LOGGER = Logger.getLogger(RpaApplication.class);

	private SimpleStringProperty titleProperty = new SimpleStringProperty();

	@Override
	public void start(Stage stage) throws Exception {
		LOGGER.info("MR-BOT Client Starting...");
		DatabaseHandler.getInstance();
		Parent root = FXMLLoader.load(getClass().getResource("/assistant/ui/login/login.fxml"));

		Scene scene = new Scene(root);

		stage.setScene(scene);
		stage.show();
		stage.setTitle("MR-BOT");

		LibraryAssistantUtil.setStageIcon(stage);
	}

	public static void main(String[] args) {
		Long startTime = System.currentTimeMillis();
		launch(args);
		Long endTime = System.currentTimeMillis();
		LOGGER.info(String.format("MR-BOT Client is started successfully on %s. Used for %s ms", LibraryAssistantUtil.formatDateTimeString(startTime), endTime));

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					JobFactory.exit();
				} catch (SchedulerException e) {
					LOGGER.error(e);
				}
				LOGGER.info(String.format("MR-RPA Client is closed on %s.", LibraryAssistantUtil.formatDateTimeString(System.currentTimeMillis())));

			}
		});
	}

}
