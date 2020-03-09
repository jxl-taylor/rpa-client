package com.mr.rpa.assistant.ui.about;

import cn.hutool.core.io.FileUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.net.URL;
import java.util.*;

@Log4j
public class VersionUpdateController implements Initializable {

	private static final String TMP_PATH = ".tmp";
	@FXML
	private StackPane rootPane;
	@FXML
	private JFXTextField updatePath;
	@FXML
	private JFXButton uploadFileButton;

	@FXML
	private AnchorPane mainContainer;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		final FileChooser fileChooser = new FileChooser();
		uploadFileButton.setOnAction((final ActionEvent e) -> {
			configureFileChooser(fileChooser);
			File file = fileChooser.showOpenDialog(uploadFileButton.getScene().getWindow());
			if (file != null) {
				//复制到更新文件夹
				String updateFileDir = GlobalProperty.getInstance().getSysConfig().getUpdatePath();
				String tmpPath = updateFileDir + File.separator + TMP_PATH;
				FileUtil.copy(file.getAbsolutePath(), tmpPath + File.separator + file.getName(), true);

				//创建临时文件夹.tmp,将安装包解压到.tmp


				/*
				复制文件
				1、数据库文件更新
				2、将原有jar文件复制到 .tmp/ 备份为 rpa_client.jar_bak，新的jar替换原来的jar
				 */

			}
		});
	}

	@FXML
	private void setup(ActionEvent event) {

	}

	@FXML
	private void cancel(ActionEvent event) {
		Stage stage = (Stage) rootPane.getScene().getWindow();
		stage.close();
	}

	private void configureFileChooser(
			final FileChooser fileChooser) {
		fileChooser.setTitle("选择安装文件");
		fileChooser.setInitialDirectory(
				new File(System.getProperty("user.home"))
		);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("所有文件", "*.*"),
				new FileChooser.ExtensionFilter("mbot", "*.mbot")
		);
	}
}
