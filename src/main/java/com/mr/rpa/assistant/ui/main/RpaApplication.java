package com.mr.rpa.assistant.ui.main;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

public class RpaApplication extends Application {

	private final static Logger LOGGER = Logger.getLogger(RpaApplication.class);

	private SimpleStringProperty titleProperty = new SimpleStringProperty();

	@Override
	public void start(Stage stage) throws Exception {
		LOGGER.info("MR-RPA Client Starting...");
		DatabaseHandler.getInstance();

		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("assistant/ui/main/main.fxml"));

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
		stage.titleProperty().bind(GlobalProperty.getInstance().getTitle());

		LibraryAssistantUtil.setStageIcon(stage);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				AlertMaker.showMaterialDialog(((StackPane) root),
						((StackPane) root).getChildren().get(0),
						GlobalProperty.getInstance().getExitBtns(), "退出", "", false);
				event.consume();
			}
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
	}

	public static void main(String[] args) {
		Long startTime = System.currentTimeMillis();
		launch(args);
		Long endTime = System.currentTimeMillis();
		LOGGER.info(String.format("MR-RPA Client is started successfully on %s. Used for %s ms", LibraryAssistantUtil.formatDateTimeString(startTime), endTime));

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
