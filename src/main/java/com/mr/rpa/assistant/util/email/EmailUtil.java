package com.mr.rpa.assistant.util.email;

import com.mr.rpa.assistant.data.callback.GenericCallback;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.log4j.Logger;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Villan
 */
public class EmailUtil {

    private final static Logger LOGGER = Logger.getLogger(EmailUtil.class);

    public static void sendTestMail(MailServerInfo mailServerInfo, String recepient, GenericCallback callback) {

        Runnable emailSendTask = () -> {
            LOGGER.info(String.format("Initiating email sending task. Sending to %s", recepient));
            Properties props = new Properties();
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.imap.ssl.trust", "*");
                props.put("mail.imap.ssl.socketFactory", sf);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", mailServerInfo.getSslEnabled() ? "true" : "false");
                props.put("mail.smtp.host", mailServerInfo.getMailServer());
                props.put("mail.smtp.port", mailServerInfo.getPort());

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailServerInfo.getEmailID(), mailServerInfo.getPassword());
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailServerInfo.getEmailID()));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(recepient));
                message.setSubject("Test mail from Library Assistant");
                message.setText("Hi,"
                        + "\n\n This is a test mail from Library Assistant!");

                Transport.send(message);
                LOGGER.info("Everything seems fine");
                callback.taskCompleted(Boolean.TRUE);
            } catch (Throwable exp) {
                LOGGER.error( "Error occurred during sending email", exp);
                callback.taskCompleted(Boolean.FALSE);
            }
        };
        Thread mailSender = new Thread(emailSendTask, "EMAIL-SENDER");
        mailSender.start();
    }

    public static void sendMail(MailServerInfo mailServerInfo, String recepient, String content, String title, GenericCallback callback) {

        Runnable emailSendTask = () -> {
            LOGGER.info(String.format("Initiating email sending task. Sending to %s", recepient));
            Properties props = new Properties();
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.imap.ssl.trust", "*");
                props.put("mail.imap.ssl.socketFactory", sf);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", mailServerInfo.getSslEnabled() ? "true" : "false");
                props.put("mail.smtp.host", mailServerInfo.getMailServer());
                props.put("mail.smtp.port", mailServerInfo.getPort());

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailServerInfo.getEmailID(), mailServerInfo.getPassword());
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailServerInfo.getEmailID()));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepient));
                message.setSubject(title);
                message.setContent(content, "text/html");

                Transport.send(message);
                LOGGER.info("Everything seems fine");
                callback.taskCompleted(Boolean.TRUE);
            } catch (Throwable exp) {
                LOGGER.error("Error occurred during sending email", exp);
                callback.taskCompleted(Boolean.FALSE);
            }
        };
        Thread mailSender = new Thread(emailSendTask, "EMAIL-SENDER");
        mailSender.start();
    }
}
