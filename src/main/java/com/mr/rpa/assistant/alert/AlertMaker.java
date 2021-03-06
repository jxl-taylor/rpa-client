package com.mr.rpa.assistant.alert;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.events.JFXDialogEvent;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class AlertMaker {

	public static void showSimpleAlert(String title, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		styleAlert(alert);
		alert.showAndWait();
	}

	public static void showErrorMessage(String title, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(title);
		alert.setContentText(content);
		styleAlert(alert);
		alert.showAndWait();
	}

	public static void showErrorMessage(Throwable ex) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("错误");
		alert.setHeaderText("发生未知系统异常");
		alert.setContentText(ex.getLocalizedMessage());

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("详细错误信息：");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);

//		styleAlert(alert);
		alert.showAndWait();
	}

	public static void showErrorMessage(Exception ex, String title, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error occured");
		alert.setHeaderText(title);
		alert.setContentText(content);

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	public static void showMaterialDialog(StackPane root, Node nodeToBeBlurred, List<JFXButton> controls, String header, String body) {
		showMaterialDialog(root, nodeToBeBlurred, controls, header, body, true);
	}

	public static void showMaterialDialog(StackPane root, Node nodeToBeBlurred, List<JFXButton> controls, String header, String body, boolean overlayClose) {
		showMaterialDialog(root, nodeToBeBlurred, controls, header, body, overlayClose, true);
	}

	public static void showMaterialDialog(StackPane root, Node nodeToBeBlurred, List<JFXButton> controls,
										  String header, String body, boolean overlayClose, boolean cancelEffectClose) {
		BoxBlur blur = new BoxBlur(3, 3, 3);
		if (controls.isEmpty()) {
//            controls.add(new JFXButton("确定"));
		}
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(root, dialogLayout, JFXDialog.DialogTransition.TOP, overlayClose);

		controls.forEach(controlButton -> {
			controlButton.getStyleClass().add("dialog-button");
			controlButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				dialog.close();
			});
		});

		dialogLayout.setHeading(new Label(header));
		dialogLayout.setBody(new Label(body));
		dialogLayout.setActions(controls);
		dialog.show();
		dialog.setOnDialogClosed((JFXDialogEvent event1) -> {
			if(cancelEffectClose) nodeToBeBlurred.setEffect(null);
		});
		nodeToBeBlurred.setEffect(blur);
	}

	public static void showMaterialDialog(StackPane root, Node nodeToBeBlurred, Node node, String header, String body, boolean overlayClose) {
		BoxBlur blur = new BoxBlur(3, 3, 3);
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(root, dialogLayout, JFXDialog.DialogTransition.TOP, overlayClose);
		dialogLayout.setBody(new Label(body));
		dialogLayout.setBody(node);
		dialog.show();
		nodeToBeBlurred.setEffect(blur);
	}

	public static void showTrayMessage(String title, String message) {
		try {
			SystemTray tray = SystemTray.getSystemTray();
			BufferedImage image = ImageIO.read(AlertMaker.class.getResource(AssistantUtil.ICON_IMAGE_LOC));
			TrayIcon trayIcon = new TrayIcon(image, "REC");
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("REC");
			tray.add(trayIcon);
			trayIcon.displayMessage(title, message, MessageType.INFO);
			tray.remove(trayIcon);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	private static void styleAlert(Alert alert) {
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		AssistantUtil.setStageIcon(stage);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(AlertMaker.class.getResource("/dark-theme.css").toExternalForm());
		dialogPane.getStyleClass().add("custom-alert");
	}
}
