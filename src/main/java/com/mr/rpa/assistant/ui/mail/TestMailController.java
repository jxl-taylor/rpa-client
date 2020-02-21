package com.mr.rpa.assistant.ui.mail;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.callback.GenericCallback;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.util.email.EmailUtil;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Villan
 */
public class TestMailController implements Initializable, GenericCallback {

    private final static Logger LOGGER = Logger.getLogger(TestMailController.class);

    @FXML
    private JFXTextField recepientAddressInput;
    @FXML
    private JFXProgressBar progressBar;

    private MailServerInfo mailServerInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setMailServerInfo(MailServerInfo mailServerInfo) {
        this.mailServerInfo = mailServerInfo;
    }

    @FXML
    private void handleStartAction(ActionEvent event) {
        String toAddress = recepientAddressInput.getText();
        if (LibraryAssistantUtil.validateEmailAddress(toAddress)) {
            EmailUtil.sendTestMail(mailServerInfo, toAddress, this);
            progressBar.setVisible(true);
        } else {
            AlertMaker.showErrorMessage("Failed", "Invalid email address!");
        }
    }

    @Override
    public Object taskCompleted(Object val) {
        LOGGER.info(String.format("Callback received from Email Sender client %s", val));
        boolean result = (boolean) val;
        Platform.runLater(() -> {
            if (result) {
                AlertMaker.showSimpleAlert("Success", "Email successfully sent!");
            } else {
                AlertMaker.showErrorMessage("Failed", "Something went wrong!");
            }
            progressBar.setVisible(false);
        });
        return true;
    }

}
