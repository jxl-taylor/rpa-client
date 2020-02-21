package com.mr.rpa.assistant.ui.notifoverdue.emailsender;

import com.jfoenix.controls.JFXProgressBar;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.callback.GenericCallback;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.database.DataHelper;
import com.mr.rpa.assistant.ui.notifoverdue.NotificationItem;
import com.mr.rpa.assistant.ui.settings.Preferences;
import com.mr.rpa.assistant.util.LibraryAssistantUtil;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Villan
 */
public class EmailSenderController implements Initializable {

    private final static Logger LOGGER = Logger.getLogger(EmailSenderController.class);

    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private Text text;

    private List<NotificationItem> list;
    private StringBuilder emailText = new StringBuilder();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Scanner scanner = new Scanner(getClass().getResourceAsStream(LibraryAssistantUtil.MAIL_CONTENT_LOC));
            while (scanner.hasNext()) {
                emailText.append(scanner.nextLine()).append("\n");
            }
            System.out.println(emailText);
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    public void setNotifRequestData(List<NotificationItem> list) {
        this.list = list;
    }

    public Stage getStage() {
        return (Stage) progressBar.getScene().getWindow();
    }

    public void start() {
        if (emailText == null || emailText.toString().isEmpty()) {
            AlertMaker.showErrorMessage("Failed", "Failed to parse email format");
            getStage().close();
        }
        new EmailSenderHelper().start();
    }

    class EmailSenderHelper extends Thread implements GenericCallback {

        private final AtomicBoolean flag = new AtomicBoolean(true);
        private final MailServerInfo mailServerInfo = DataHelper.loadMailServerInfo();

        @Override
        public void run() {
            final int size = list.size();
            int count = 0;

            Iterator iterator = list.iterator();
            while (iterator.hasNext() && flag.get()) {
                count++;
                NotificationItem item = (NotificationItem) iterator.next();
                String reportDate = LibraryAssistantUtil.getDateString(new Date());
                String bookName = item.getBookName();
                String issueDate = item.getIssueDate();
                Integer daysUsed = item.getDayCount();
                String finePerDay = String.valueOf(Preferences.getPreferences().getFinePerDay());
                String amount = item.getFineAmount();
                String emailContent = String.format(emailText.toString(), reportDate, bookName, issueDate, daysUsed, finePerDay, amount);
//                EmailUtil.sendMail(mailServerInfo, item.getMemberEmail(), emailContent, "Library Assistant Overdue Notification", this);
                flag.set(false);
                updateUI(size, count);
            }
            Platform.runLater(() -> {
                text.setText("Process Completed!");
                progressBar.setProgress(1);
            });
        }

        @Override
        public Object taskCompleted(Object val) {
            flag.set(true);
            return null;
        }

        private void updateUI(int size, int count) {
            Platform.runLater(() -> {
                text.setText(String.format("Notifying %d/%d", count, size));
                progressBar.setProgress((double) count / (double) size);
            });
        }
    }

}
