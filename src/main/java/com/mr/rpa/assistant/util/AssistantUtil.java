package com.mr.rpa.assistant.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.mr.rpa.assistant.ui.settings.Preferences;
import com.mr.rpa.assistant.ui.main.MainController;

public class AssistantUtil {

	public static final String ICON_IMAGE_LOC = "/icon.png";
	public static final String MAIL_CONTENT_LOC = "/mail_content.html";
	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	private static HashMap<String, Pair<Stage, Object>> stages = Maps.newHashMap();

	public static void setStageIcon(Stage stage) {
		stage.getIcons().add(new Image(ICON_IMAGE_LOC));
	}

	public static Pair<Stage, Object> loadWindow(URL loc, String title, Stage parentStage) {
		return loadWindow(loc, title, parentStage, true);
	}

	public static Pair<Stage, Object> loadWindow(URL loc, String title, Stage parentStage, boolean isSingleton) {
		if (isSingleton) {
			if (stages.get(loc.getPath()) != null) {
				Stage stage = stages.get(loc.getPath()).getObject1();
				if (stage != null && stage.isShowing()) {
					stage.requestFocus();
					return stages.get(loc.getPath());
				}
			}
		}
		Object controller = null;
		try {
			FXMLLoader loader = new FXMLLoader(loc);
			Parent parent = loader.load();
			controller = loader.getController();
			Stage stage = null;
			if (parentStage != null) {
				stage = parentStage;
			} else {
				stage = new Stage(StageStyle.DECORATED);
			}
			stage.setTitle(title);
			stage.setScene(new Scene(parent));
			stage.show();
			setStageIcon(stage);
			stage.setResizable(false);
			stage.setFullScreen(false);
			if (isSingleton) stages.put(loc.getPath(), new Pair<>(stage, controller));
		} catch (IOException ex) {
			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return stages.get(loc.getPath());
	}

	public static Pair<Stage, Object> getWindow(URL loc) {
		return stages.get(loc.getPath());
	}

	public static void closeWinow(URL loc) {
		Stage stage = stages.get(loc.getPath()).getObject1();
		if (stage != null && stage.isShowing()) {
			stage.close();
			stages.remove(loc.getPath());
		}
	}
	public static String formatDateTimeString(Date date) {
		return DATE_TIME_FORMAT.format(date);
	}

	public static String formatDateTimeString(Long time) {
		return DATE_TIME_FORMAT.format(new Date(time));
	}

	public static String getDateString(Date date) {
		return DATE_FORMAT.format(date);
	}

	public static boolean validateEmailAddress(String emailID) {
		String regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(emailID).matches();
	}

	public static void openFileWithDesktop(File file) {
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} catch (IOException ex) {
			Logger.getLogger(AssistantUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
