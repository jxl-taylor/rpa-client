package com.mr.rpa.assistant.ui.main.toolbar;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.Pair;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ToolbarController implements Initializable {

    @FXML
    private JFXButton loginButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void loadAddTask(ActionEvent event) {
        AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/addtask/add_task.fxml"), "添加任务", null);
    }

    @FXML
    private void loadUpdate(ActionEvent event) {
        Pair<Stage, Object> pair = AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/about/version_update.fxml"), "更新", null);
        pair.getObject1().setOnCloseRequest((e) -> {
            AssistantUtil.closeWinow(getClass().getClassLoader().getResource("assistant/ui/about/version_update.fxml"));
        });
    }

    @FXML
    private void exit(ActionEvent event) {
        JFXButton confirmBtn = new JFXButton("确定");
        confirmBtn .addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            System.exit(0);
        });

        JFXButton cancelBtn = new JFXButton("取消");
        cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            StackPane rootPane = GlobalProperty.getInstance().getRootPane();
            rootPane.getChildren().get(0).setEffect(null);
        });
        StackPane rootPane = GlobalProperty.getInstance().getRootPane();
        AlertMaker.showMaterialDialog(rootPane,
                rootPane.getChildren().get(0),
                Lists.newArrayList(confirmBtn, cancelBtn), "退出", "", false);
    }

    @FXML
    private void LoadAboutUs(ActionEvent event) {
        AssistantUtil.loadWindow(getClass().getClassLoader().getResource("assistant/ui/about/about.fxml"), "关于我们", null);
    }

    @FXML
    private void loadSettings(ActionEvent event) {
        GlobalProperty globalProperty = GlobalProperty.getInstance();
        globalProperty.getTaskPaneVisible().setValue(false);
        globalProperty.getSettingPaneVisible().setValue(true);
        globalProperty.getMyInfoPaneVisible().setValue(false);
    }

}
