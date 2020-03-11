package com.mr.rpa.assistant.ui.about;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.job.JobFactory;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.quartz.SchedulerException;

import java.io.*;
import java.net.URL;
import java.util.*;

@Log4j
public class VersionUpdateController implements Initializable {

	private static final String TMP_PATH = ".tmp";

	private static final String BAK_PATH = "bak";

	private static final String COLON = ":";

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

	private ArrayList<JFXButton> btns = new ArrayList<>();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initExitBtn();
		updatePath.setText("");
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		if (FileUtil.exist(updateFileDir + File.separator + BAK_PATH + File.separator + SystemContants.UPDATE_CHECK_LIST)) {
			rollbackBtn.setDisable(false);
		} else {
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
	private void rollback(ActionEvent event) throws IOException {
		//将/update/bak/rpa-client.jar 复制回去
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		String bakDir = updateFileDir + File.separator + BAK_PATH;
		Properties bakListProp = new Properties();
		BufferedInputStream inputStream = FileUtil.getInputStream(bakDir + File.separator + SystemContants.UPDATE_CHECK_LIST);
		try {
			bakListProp.load(inputStream);
			for (Object key : bakListProp.keySet()) {
				CommonUtil.copyAndCoverFile(bakDir + File.separator + key,
						String.valueOf(key).replace(SystemContants.SEPARATER, File.separator)
								.replace(SystemContants.COLON, COLON));
			}
		} catch (Exception e) {
			log.error(e);
			AlertMaker.showSimpleAlert("失败", "恢复失败");
			return;
		}finally {
			inputStream.close();
		}

		FileUtil.del(bakDir);
//		Platform.runLater(() -> {
//			FileUtil.del(bakDir);
//		});
		//提示重启
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "回退成功，重启后生效",
				new ButtonType("退出", ButtonBar.ButtonData.YES));
//        设置窗口的标题
		alert.setTitle("回退");
		alert.setHeaderText("回退成功");
		alert.showAndWait();
		System.exit(0);
	}

	private void initExitBtn() {

		JFXButton exitBtn = new JFXButton("确定");
		exitBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
			try {
				JobFactory.pauseAll();
			} catch (SchedulerException e) {
				log.error(e);
				AlertMaker.showSimpleAlert("失败", "调度器停止失败");
			}
			System.exit(0);
		});
		btns.add(exitBtn);
	}

	@FXML
	private void setup(ActionEvent event) throws IOException {
		boolean needRestart = false;
		File file = new File(updatePath.getText());
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		String updateFileDir = sysConfig.getUpdatePath();
		//将上传文件放在update/下面
		FileUtil.copy(file.getAbsolutePath(), updateFileDir + File.separator + file.getName(), true);
		//解压更新文件到/update/.tmp/下
		String tmpPath = updateFileDir + File.separator + TMP_PATH;
		FileUtil.del(tmpPath);
		int index = file.getName().indexOf(".");
		String srcSuffixName = file.getName().substring(0, index) + ".zip";
		String zipPath = tmpPath + File.separator + srcSuffixName;
		FileUtil.copy(file.getAbsolutePath(), zipPath, true);
		//将安装包解压到.tmp， 解压后的更新文件夹放在tmpPath下
		ZipUtil.unzip(zipPath, tmpPath);

		//更新文件夹的全路径
		String theUpdateDir = "";
		String[] fileNames = new File(tmpPath).list();
		for (int i = 0; i < fileNames.length; i++) {
			File setupDir = new File(tmpPath + File.separator + fileNames[i]);
			if (setupDir.isDirectory()) theUpdateDir = setupDir.getAbsolutePath();
		}
		if (StringUtils.isEmpty(theUpdateDir)) {
			log.error("更新文件错误,更新文件是一个文件夹结构");
			AlertMaker.showSimpleAlert("失败", "更新失败");
			return;
		}
		if (!FileUtil.exist(theUpdateDir + File.separator + SystemContants.UPDATE_CHECK_LIST)) {
			log.error("更新文件错误,找不到 checklist.properties");
			AlertMaker.showSimpleAlert("失败", "更新失败");
			return;
		}

		Properties checkListProp = new Properties();
		BufferedInputStream inputStream = FileUtil.getInputStream(theUpdateDir + File.separator + SystemContants.UPDATE_CHECK_LIST);
		try {
			checkListProp.load(inputStream);
			needRestart = updateByCheckList(checkListProp, theUpdateDir, updateFileDir + File.separator + BAK_PATH, sysConfig.getBotRootDir());
		} catch (Exception e) {
			log.error(e);
			AlertMaker.showSimpleAlert("失败", "更新失败, 原因：" + e.getMessage());
			return;
		}finally {
			inputStream.close();
		}
		FileUtil.del(tmpPath);
//
//		Platform.runLater(() -> {
//			FileUtil.del(tmpPath);
//		});

		if (!needRestart) {
			AlertMaker.showSimpleAlert("更新", "更新成功");
			cancel(null);
			return;
		}
		//提示重启
		try {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "更新成功，请重启后生效",
					new ButtonType("退出", ButtonBar.ButtonData.YES));
//        设置窗口的标题
			alert.setTitle("更新");
			alert.setHeaderText("更新成功");
//			howAndWait() 将在对话框消失以前不会执行之后的代码
			alert.showAndWait();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}


	}

	private boolean updateByCheckList(Properties checkListProp, String theUpdateDir, String bakDir, String rootDir) throws Exception {
		boolean needRestart = false;
		Properties bakProps = new Properties();
		for (Object key : checkListProp.keySet()) {
			String sKey = String.valueOf(key);
			String[] keyArray = sKey.split("\\|");
			if (keyArray[0].startsWith(SystemContants.UPDATE_CHECK_LIST_TYPE_SQL)) {
				executeUpdateSqlFile(new File(theUpdateDir + File.separator + keyArray[1]));
			} else if (keyArray[0].startsWith(SystemContants.UPDATE_CHECK_LIST_TYPE_FILE)) {
				needRestart = true;
				String pathValue = getPropValue(checkListProp, key);
				String desFilePath = rootDir
						+ (StringUtils.isEmpty(pathValue) ? "" : File.separator + pathValue)
						+ File.separator + keyArray[1];
				if (FileUtil.exist(desFilePath)) {
					bakProps.put(convertPath(desFilePath), desFilePath);
					//备份、将文件放在update/bak下面
					FileUtil.copy(desFilePath, bakDir + File.separator + convertPath(desFilePath), true);
					//替换文件
					CommonUtil.copyAndCoverFile(theUpdateDir + File.separator + keyArray[1],
							desFilePath);
				} else {
					//替换文件
					FileUtil.copy(theUpdateDir + File.separator + keyArray[1],
							desFilePath, true);
				}

			} else if (keyArray[0].startsWith(SystemContants.UPDATE_CHECK_LIST_TYPE_DIR)) {
				needRestart = true;
				String pathValue = getPropValue(checkListProp, key);
				copyFilesFromDir(bakDir,
						theUpdateDir + File.separator + keyArray[1],
						rootDir + (StringUtils.isEmpty(pathValue) ? "" : File.separator + pathValue),
						bakProps);
			}

		}
		//save bak checklist
		if(bakProps.size() > 0){
			FileUtil.writeUtf8String(CommonUtil.toPropString(bakProps),
					bakDir + File.separator + SystemContants.UPDATE_CHECK_LIST);
		}
		return needRestart;
	}

	private Object convertPath(String desFilePath) {
		return desFilePath
				.replace(File.separator, SystemContants.SEPARATER)
				.replace(COLON, SystemContants.COLON);
	}

	private String getPropValue(Properties checkListProp, Object key) {
		Object v = checkListProp.get(String.valueOf(key));
		if (v == null) return "";
		String value = String.valueOf(v).trim();

		if (value.startsWith(".")) value = value.substring(1);
		else if (value.startsWith("/")) value = value.substring(1);
		if (value.endsWith("/")) value = value.substring(0, value.length() - 1);

		if (value.startsWith("\\")) value = value.substring(1);
		if (value.endsWith("\\")) value = value.substring(0, value.length() - 1);

		return value;
	}

	/**
	 * @param bakDir     备份文件夹全路径
	 * @param currentDir 当前处理文件夹全路径
	 * @param desPath    复制目的地的全路径
	 */
	private void copyFilesFromDir(String bakDir, String currentDir, String desPath, Properties bakProps) throws Exception {
		String[] fileNames = new File(currentDir).list();
		for (int i = 0; i < fileNames.length; i++) {
			File setupFile = new File(currentDir + File.separator + fileNames[i]);
			if (setupFile.isDirectory()) {
				copyFilesFromDir(bakDir, setupFile.getAbsolutePath(), desPath + File.separator + fileNames[i], bakProps);
			} else {
				String desFilePath = desPath + File.separator + fileNames[i];
				if (FileUtil.exist(desFilePath)) {
					bakProps.put(convertPath(desFilePath), desFilePath);
					//备份、将文件放在update/bak下面
					FileUtil.copy(desFilePath, bakDir + File.separator + convertPath(desFilePath), true);
					//替换文件
					CommonUtil.copyAndCoverFile(setupFile.getAbsolutePath(), desFilePath);
				} else {
					//替换文件
					FileUtil.copy(setupFile.getAbsolutePath(), desFilePath, true);
				}


			}
		}
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
