package com.mr.rpa.assistant.ui.about;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.util.*;

@Log4j
public class VersionUpdateController implements Initializable {

	private static final String TMP_PATH = ".tmp";

	private static final String BAK_PATH = "bak";

	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;
	@FXML
	private JFXTextField updatePath;
	@FXML
	private JFXButton uploadFileButton;

	@FXML
	private JFXButton setupBtn;

	@FXML
	private JFXButton rollbackBtn;

	private JFXButton exitBtn;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initExitBtn();
		updatePath.setText("");
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		if(FileUtil.exist(updateFileDir + File.separator + BAK_PATH + File.separator + SystemContants.JAR_NAME)){
			rollbackBtn.setDisable(false);
		}else {
			rollbackBtn.setDisable(true);
		}

		final FileChooser fileChooser = new FileChooser();
		uploadFileButton.setOnAction((final ActionEvent e) -> {
			configureFileChooser(fileChooser);
			File file = fileChooser.showOpenDialog(uploadFileButton.getScene().getWindow());
			if (file != null) {
				updatePath.setText(file.getAbsolutePath());
				setupBtn.setDisable(false);
			}
		});
		setupBtn.setDisable(true);
	}

	private void executeUpdateSqlFile(File sqlSetupFile) {
		ScriptRunner runner = new ScriptRunner(DatabaseHandler.getInstance().getConnection());
//		runner.setLogWriter(null);//设置是否输出日志
		Reader read = FileUtil.getUtf8Reader(sqlSetupFile);
		runner.runScript(read);
	}

	@FXML
	private void rollback(ActionEvent event) {
		//将/update/bak/rpa-client.jar 复制回去
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		String bakFile = updateFileDir + File.separator + BAK_PATH + File.separator + SystemContants.JAR_NAME;
		FileUtil.copy(bakFile, sysConfig.getJarFilePath(), true);
		FileUtil.del(bakFile);
		//提示重启
		AlertMaker.showMaterialDialog(((StackPane) rootPane),
				((StackPane) rootPane).getChildren().get(0),
				Lists.newArrayList(exitBtn), "更新", "回退成功，请重启", false);
	}

	private void initExitBtn(){
		exitBtn = new JFXButton("确定");
		exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				JobFactory.pauseAll();
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showSimpleAlert("失败", "调度器停止失败");
			}
			System.exit(0);
		});
	}

	@FXML
	private void setup(ActionEvent event) {
		boolean needRestart = false;
		File file = new File(updatePath.getText());
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		//将上传文件放在update/下面
		FileUtil.copy(file.getAbsolutePath(), updateFileDir + File.separator + file.getName(), true);
		//将原来的jar文件放在update/bak下面
		if(FileUtil.exist(sysConfig.getJarFilePath())){
			FileUtil.copy(sysConfig.getJarFilePath(),
					updateFileDir + File.separator + BAK_PATH + File.separator + SystemContants.JAR_NAME , true);
		}

		//解压更新文件到/update/.tmp/下
		String tmpPath = updateFileDir + File.separator + TMP_PATH;
		FileUtil.del(tmpPath);
		int index = file.getName().indexOf(".");
		String srcSuffixName = file.getName().substring(0, index) + ".zip";
		String zipPath = tmpPath + File.separator + srcSuffixName;
		FileUtil.copy(file.getAbsolutePath(), zipPath, true);
		//将安装包解压到.tmp
		ZipUtil.unzip(zipPath, tmpPath);
		String[] fileNames = new File(tmpPath).list();
		for (int i = 0; i < fileNames.length; i++) {
			File setupDir = new File(tmpPath + File.separator + fileNames[i]);
			if (setupDir.isDirectory()) {
				String[] setupFileNames = setupDir.list();
				for (int j = 0; j < setupFileNames.length; j++) {
					File setupFile = new File(setupDir.getAbsolutePath() + File.separator + setupFileNames[i]);
					if (setupFileNames[i].endsWith(".sql")) {    //1、数据库文件更新
						executeUpdateSqlFile(setupFile);
					} else if (setupFileNames[i].equalsIgnoreCase(SystemContants.JAR_NAME)) {    //2、新的jar替换原来的jar]
						FileUtil.copy(setupDir.getAbsolutePath() + File.separator + SystemContants.JAR_NAME,
								sysConfig.getJarFilePath(), true);
						needRestart = true;
					}
					//todo 可增加别的需要copy的文件
				}
			}
		}
		FileUtil.del(tmpPath);
		if (!needRestart) {
			AlertMaker.showSimpleAlert("更新", "更新成功");
			cancel(null);
			return;
		}
		//提示重启
		AlertMaker.showMaterialDialog(((StackPane) rootPane),
				((StackPane) rootPane).getChildren().get(0),
				Lists.newArrayList(exitBtn), "更新", "更新成功，请重启", false);

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
