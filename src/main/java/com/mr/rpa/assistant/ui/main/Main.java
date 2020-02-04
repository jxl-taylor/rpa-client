package com.mr.rpa.assistant.ui.main;

import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.application.Application;
import static javafx.application.Application.launch;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.exceptions.ExceptionUtil;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private final static Logger LOGGER = LogManager.getLogger(Main.class.getName());

    private SimpleStringProperty titleProperty = new SimpleStringProperty();
    @Override
    public void start(Stage stage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("assistant/ui/login/login.fxml"));
//
//        Scene scene = new Scene(root);
//
//        stage.setScene(scene);
//        stage.show();
//        stage.setTitle("MR ROBOT Login");
//
//        LibraryAssistantUtil.setStageIcon(stage);

//        new Thread(() -> {
//            ExceptionUtil.init();
//            DatabaseHandler.getInstance();
//        }).start();

        ExceptionUtil.init();
        DatabaseHandler.getInstance();

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("assistant/ui/main/main.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        stage.titleProperty().bind(GlobalProperty.getInstance().titleProperty());

        LibraryAssistantUtil.setStageIcon(stage);

    }

    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Library Assistant launched on {}", LibraryAssistantUtil.formatDateTimeString(startTime));
        launch(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Long exitTime = System.currentTimeMillis();
                LOGGER.log(Level.INFO, "Library Assistant is closing on {}. Used for {} ms", LibraryAssistantUtil.formatDateTimeString(startTime), exitTime);
            }
        });
    }

}
