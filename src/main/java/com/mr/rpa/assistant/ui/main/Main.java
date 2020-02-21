package com.mr.rpa.assistant.ui.main;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.exceptions.ExceptionUtil;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

public class Main extends Application {

    private final static Logger LOGGER = Logger.getLogger(Main.class);

    private SimpleStringProperty titleProperty = new SimpleStringProperty();
    @Override
    public void start(Stage stage) throws Exception {
//        ExceptionUtil.init();
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
                AlertMaker.showMaterialDialog(((StackPane)root),
                        ((StackPane)root).getChildren().get(0),
                        GlobalProperty.getInstance().getExitBtns(), "退出", "", false);
                event.consume();
            }
        });

        LOGGER.info("MR-RPA Client Started successfully");
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                stage.setIconified(true);
//            }
//        });
        new Thread(()->{
            for(;;){
                LOGGER.info("MR-RPA TEST LOG................................");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        launch(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Long exitTime = System.currentTimeMillis();
                LOGGER.error( String.format("MR-RPA Client is closing on %s. Used for %s ms", LibraryAssistantUtil.formatDateTimeString(startTime), exitTime));
            }
        });
    }

}
