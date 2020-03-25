package com.mr.rpa.assistant.util.email;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.callback.GenericCallback;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.sun.mail.util.MailSSLSocketFactory;
import javafx.application.Platform;
import jdk.nashorn.internal.objects.Global;
import lombok.extern.log4j.Log4j;

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
@Log4j
public class EmailUtil {

	private static GenericCallback gbCallback = new BgCallback();

	private static GenericCallback showCallback = new ShowCallback();

	public static void sendTestMail(MailServerInfo mailServerInfo, String recepient) {

		Runnable emailSendTask = () -> {
			log.info(String.format("Initiating email sending task. Sending to %s", recepient));
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
				message.setSubject("来自REC的测试邮件");
				message.setText("您好,"
						+ "\n\n 这是一封RPA机器人大麦的测试邮件!");

				Transport.send(message);
				log.info("测试邮件发送成功");
				showCallback.taskCompleted(Boolean.TRUE);
			} catch (Throwable exp) {
				log.error("Error occurred during sending email", exp);
				showCallback.taskCompleted(Boolean.FALSE);
			}
		};
		Thread mailSender = new Thread(emailSendTask, "EMAIL-SENDER");
		mailSender.start();
	}

	public static void sendMail(MailServerInfo mailServerInfo, String recepient, String content, String title, GenericCallback callback) {
		Runnable emailSendTask = () -> {
			log.info(String.format("Initiating email sending task. Sending to %s", recepient));
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
//				message.setContent(content, "text/html");
				message.setText(content);
				Transport.send(message);
				log.info("Everything seems fine");
				callback.taskCompleted(Boolean.TRUE);
			} catch (Throwable exp) {
				log.error("Error occurred during sending email", exp);
				callback.taskCompleted(Boolean.FALSE);
			}
		};
		Thread mailSender = new Thread(emailSendTask, "EMAIL-SENDER");
		mailSender.start();
	}

	public static void sendBgMail(MailServerInfo mailServerInfo, String recepient, String content, String title) {
		sendMail(mailServerInfo, recepient, content, title, gbCallback);
	}

	public static void sendBgMail(String recepient, String content, String title) {
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		MailServerInfo mailServerInfo = new MailServerInfo(sysConfig.getMailServerName(),
				sysConfig.getMailSmtpPort(),
				sysConfig.getMailEmailAddress(), sysConfig.getMailEmailPassword(), sysConfig.getMailSslCheckbox());
		sendBgMail(mailServerInfo, recepient, content, title);
	}

	public static void sendShowMail(MailServerInfo mailServerInfo, String recepient, String content, String title) {
		sendMail(mailServerInfo, recepient, content, title, showCallback);
	}

	static class BgCallback implements GenericCallback {
		@Override
		public Object taskCompleted(Object val) {
			boolean result = (boolean) val;
			Platform.runLater(() -> {
				if (!result) {
					log.error("邮件发送失败!");
				}
			});
			return true;
		}
	}

	static class ShowCallback implements GenericCallback {
		@Override
		public Object taskCompleted(Object val) {
			boolean result = (boolean) val;
			Platform.runLater(() -> {
				if (result) {
					AlertMaker.showSimpleAlert("Success", "邮件发送成功!");
				} else {
					AlertMaker.showErrorMessage("Failed", "邮件发送失败!");
				}
			});
			return true;
		}
	}
}
