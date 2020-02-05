package com.mr.rpa.assistant.ui.settings;

import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.ui.settings.Preferences;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.export.DatabaseExporter;
import com.mr.rpa.assistant.ui.mail.TestMailController;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import javafx.stage.Window;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingsController implements Initializable {

    @FXML
    private JFXTextField adminUsername;
    @FXML
    private JFXTextField mailServerName;
    @FXML
    private JFXTextField mailSmtpPort;
    @FXML
    private JFXTextField mailEmailAddress;

    private final static Logger LOGGER = LogManager.getLogger(DatabaseHandler.class.getName());
    @FXML
    private JFXPasswordField mailEmailPassword;
    @FXML
    private JFXCheckBox mailSslCheckbox;
    @FXML
    private JFXTextField taskFilePath;
    @FXML
    private JFXTextField logPath;
    @FXML
    private JFXTextField controlServer;
    @FXML
    private JFXTextField dbPath;
    @FXML
    private JFXTextField miniteErrorLimit;
    @FXML
    private JFXTextField runningLimit;
    @FXML
    private JFXSpinner progressSpinner;

    private SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        adminUsername.setText(sysConfig.getAdminUsername());
        adminUsername.setEditable(false);
        mailServerName.setText(sysConfig.getMailServerName());
        mailSmtpPort.setText(sysConfig.getMailSmtpPort().toString());
        mailEmailAddress.setText(sysConfig.getMailEmailAddress());
        mailEmailPassword.setText(sysConfig.getMailEmailPassword());
        mailSslCheckbox.setSelected(sysConfig.getMailSslCheckbox());

        taskFilePath.setText(sysConfig.getTaskFilePath());
        logPath.setText(sysConfig.getLogPath());
        controlServer.setText(sysConfig.getControlServer());

        dbPath.setText(sysConfig.getDbPath());
        dbPath.setEditable(false);

        miniteErrorLimit.setText(sysConfig.getMiniteErrorLimit().toString());
        runningLimit.setText(sysConfig.getRunningLimit().toString());

    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        sysConfig.setMailServerName(mailServerName.getText());
        sysConfig.setMailSmtpPort(Integer.parseInt(mailSmtpPort.getText()));
        sysConfig.setMailEmailAddress(mailEmailAddress.getText());
        sysConfig.setMailEmailPassword(mailEmailPassword.getText());
        sysConfig.setMailSslCheckbox(mailSslCheckbox.isSelected());
        DatabaseHandler.getInstance().updateSysConfig();
        AlertMaker.showSimpleAlert("保存", "基础配置修改成功");
    }

    @FXML
    private void handleSaverRunningAction(ActionEvent event) {
       sysConfig.setTaskFilePath(taskFilePath.getText());
       sysConfig.setLogPath(logPath.getText());
       sysConfig.setControlServer(controlServer.getText());
        DatabaseHandler.getInstance().updateSysConfig();
        AlertMaker.showSimpleAlert("保存", "任务配置修改成功");
    }

    @FXML
    private void handleSaveAlertAction(ActionEvent event) {
        sysConfig.setMiniteErrorLimit(Integer.parseInt(miniteErrorLimit.getText()));
        sysConfig.setRunningLimit(Integer.parseInt( runningLimit.getText()));
        DatabaseHandler.getInstance().updateSysConfig();
        AlertMaker.showSimpleAlert("保存", "预警配置修改成功");
    }


    @FXML
    private void handleDatabaseExportAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Location to Create Backup");
        File selectedDirectory = directoryChooser.showDialog(getStage());
        if (selectedDirectory == null) {
            AlertMaker.showErrorMessage("Export cancelled", "No Valid Directory Found");
        } else {
            DatabaseExporter databaseExporter = new DatabaseExporter(selectedDirectory);
            progressSpinner.visibleProperty().bind(databaseExporter.runningProperty());
            new Thread(databaseExporter).start();
        }
    }

    private Stage getStage() {
        return (Stage) taskFilePath.getScene().getWindow();
    }
}
