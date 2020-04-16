package com.mr.rpa.assistant.ui.listtask;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

@Log4j
public class BotMarketListController implements Initializable {
	@FXML
	private JFXTextField botName;
	@FXML
	private JFXTextField version;

	@FXML
	private TableView<Bot> tableView;
	@FXML
	private TableColumn<Bot, Integer> seqCol;
	@FXML
	private TableColumn<Bot, String> botNameCol;
	@FXML
	private TableColumn<Bot, String> mainBotCol;
	@FXML
	private TableColumn<Bot, String> despCol;
	@FXML
	private TableColumn<Bot, String> versionCol;
	@FXML
	private TableColumn<Bot, String> downloadUrlCol;
	@FXML
	private TableColumn<Bot, String> createdByCol;
	@FXML
	private TableColumn<Bot, String> createdTimeCol;
	@FXML
	private TableColumn<Bot, HBox> operatingCol;
	@FXML
	private StackPane rootPane;
	@FXML
	private AnchorPane mainContainer;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	private TaskLogService taskLogService = ServiceFactory.getService(TaskLogService.class);

	private GlobalProperty globalProperty = GlobalProperty.getInstance();

	private static ObservableList<Bot> botList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initCol();
		tableView.setItems(botList);
		refreshBotList();
	}

	public void refreshBotList() {
		refreshBotList(null);
	}

	public void refreshBotList(String botName) {
		refreshBotList(botName, null);
	}

	public void refreshBotList(String botName, String verison) {
		botList.clear();
		SysConfig sysConfig = globalProperty.getSysConfig();
		if (StringUtils.isEmpty(sysConfig.getControlServer())) {
			AlertMaker.showErrorMessage("BOT市场", "控制中心未配置");
		}
		Map<String, String> jsonMap = Maps.newHashMap();
		jsonMap.put("botName", botName);
		jsonMap.put("verison", verison);
		try {
			CommonUtil.requestControlCenter(sysConfig.getControlServer(),
					SystemContants.API_SERVICE_ID_BOT_MARKET_QUERY,
					JSON.toJSONString(jsonMap),
					resultJson -> {
						JSONArray jsonArray = resultJson.getJSONArray("botContent");
						if (jsonArray == null) return "";
						for (int i = 0; i < jsonArray.size(); i++) {
							JSONObject botObj = jsonArray.getJSONObject(i);
							botList.add(new Bot(i + 1, botObj.getString("botName"),
									botObj.getString("mainBot"),
									botObj.getString("desp"),
									botObj.getString("version"),
									botObj.getString("downloadUrl"),
									botObj.getString("createdBy"),
									botObj.getString("createdTime")));
						}
						return "";
					});
		} catch (Throwable e) {
			log.error(e);
			AlertMaker.showErrorMessage(e);
		}
	}

	private Stage getStage() {
		return (Stage) tableView.getScene().getWindow();
	}

	private void initCol() {
		seqCol.setCellValueFactory(new PropertyValueFactory<>("seq"));
		botNameCol.setCellValueFactory(new PropertyValueFactory<>("botName"));
		mainBotCol.setCellValueFactory(new PropertyValueFactory<>("mainBot"));
		despCol.setCellValueFactory(new PropertyValueFactory<>("desp"));
		versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
		downloadUrlCol.setCellValueFactory(new PropertyValueFactory<>("downloadUrl"));
		createdByCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
		createdTimeCol.setCellValueFactory(new PropertyValueFactory<>("createdTime"));
		operatingCol.setCellValueFactory(new PropertyValueFactory<>("operatingBox"));
	}

	@FXML
	private void loadBotMarket(ActionEvent event) {
		if(StringUtils.isEmpty(botName.getText()) && StringUtils.isNotEmpty(version.getText())){
			AlertMaker.showMaterialDialog(rootPane, mainContainer, new ArrayList<>(),
					"输入有误", "通过版本号查询时BOT名称不能为空");
			return;
		}
		refreshBotList(botName.getText(), version.getText());
	}

	@FXML
	public void clearSearch(ActionEvent event) {
		botName.setText("");
		version.setText("");
		refreshBotList();
	}

	public class Bot {
		private final SimpleIntegerProperty seq;
		private final SimpleStringProperty botName;
		private final SimpleStringProperty mainBot;
		private final SimpleStringProperty desp;
		private final SimpleStringProperty version;
		private final SimpleStringProperty downloadUrl;
		private final SimpleStringProperty createdBy;
		private final SimpleStringProperty createdTime;

		private HBox operatingBox;

		private Hyperlink downloadLink = new Hyperlink("下载");
		private Hyperlink updateLink = new Hyperlink("更新");

		public Bot(int seq, String botName, String mainBot, String desp, String version, String downloadUrl, String createdBy, String createdTime) {
			this.seq = new SimpleIntegerProperty(seq);
			this.botName = new SimpleStringProperty(botName);
			this.mainBot = new SimpleStringProperty(mainBot);
			this.desp = new SimpleStringProperty(desp);
			this.version = new SimpleStringProperty(version);
			this.downloadUrl = new SimpleStringProperty(downloadUrl);
			this.createdBy = new SimpleStringProperty(createdBy);
			this.createdTime = new SimpleStringProperty(createdTime);
			initOperatingBox();
		}

		private void initOperatingBox() {
			downloadLink.setOnAction(event -> {
				downloadLink.setText("正在下载");
				downloadLink.setDisable(true);
				try {
					dowloadAndInstallBot();
					Task task = new Task(UUID.randomUUID().toString().replace("-", ""),
							getBotName(), getMainBot(), getDesp(), "[]", "",
							false, SystemContants.TASK_RUNNING_STATUS_RUN, "0 0 0/1 * * ?",
							true, getVersion(),
							0, 0, null, new Timestamp(System.currentTimeMillis()));
					taskService.insertNewTask(task);
					AlertMaker.showSimpleAlert("BOT下载", "下载完成");
					TaskListController controller = GlobalProperty.getInstance().getTaskListController();
					controller.loadData();
					operatingBox.getChildren().remove(downloadLink);
				} catch (Exception e) {
					log.error(e);
					AlertMaker.showErrorMessage(e);
					downloadLink.setText("下载");
					downloadLink.setDisable(false);
					return;
				}
			});
			updateLink.setOnAction(event -> {
				updateLink.setText("正在更新");
				updateLink.setDisable(true);
				try {
					dowloadAndInstallBot();
					Task task = taskService.queryTaskByName(getBotName());
					task.setVersion(getVersion());
					taskService.updateTask(task);
					AlertMaker.showSimpleAlert("BOT更新", "更新成功");
					//如果不是最新版本，则可以继续更新
					Map<String, String> jsonMap = Maps.newHashMap();
					jsonMap.put("botName", getBotName());
					SysConfig sysConfig = globalProperty.getSysConfig();
					CommonUtil.requestControlCenter(sysConfig.getControlServer(),
							SystemContants.API_SERVICE_ID_BOT_MARKET_QUERY,
							JSON.toJSONString(jsonMap),
							resultJson -> {
								JSONArray jsonArray = resultJson.getJSONArray("botContent");
								JSONObject botObj = jsonArray.getJSONObject(0);
								if (!botObj.getString("version").equals(task.getVersion())) {
									updateLink.setText("更新");
									updateLink.setDisable(false);
								} else {
									operatingBox.getChildren().remove(updateLink);
								}
								return "";
							});
				} catch (Throwable e) {
					log.error(e);
					AlertMaker.showErrorMessage(e);
				}
			});
			operatingBox = new HBox();
			Task task = taskService.queryTaskByName(getBotName());
			if (task != null) {
				if (!getVersion().equals(task.getVersion()))
					operatingBox.getChildren().add(updateLink);
			} else {
				operatingBox.getChildren().add(downloadLink);
			}
			operatingBox.setSpacing(20);
			operatingBox.setAlignment(Pos.CENTER);
		}

		public void dowloadAndInstallBot() throws Exception {
			SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
			String botZipPath = System.getProperty("user.dir")
					+ File.separator
					+ getBotName() + getVersion() + ".zip";
			String outFileDir = sysConfig.getTaskFilePath();
			CommonUtil.downloadAndUnzip(getDownloadUrl(), botZipPath, outFileDir);
		}

		public int getSeq() {
			return seq.get();
		}

		public SimpleIntegerProperty seqProperty() {
			return seq;
		}

		public String getBotName() {
			return botName.get();
		}

		public SimpleStringProperty botNameProperty() {
			return botName;
		}

		public String getMainBot() {
			return mainBot.get();
		}

		public SimpleStringProperty mainBotProperty() {
			return mainBot;
		}

		public String getDesp() {
			return desp.get();
		}

		public SimpleStringProperty despProperty() {
			return desp;
		}

		public String getVersion() {
			return version.get();
		}

		public SimpleStringProperty versionProperty() {
			return version;
		}

		public String getDownloadUrl() {
			return downloadUrl.get();
		}

		public SimpleStringProperty downloadUrlProperty() {
			return downloadUrl;
		}

		public String getCreatedBy() {
			return createdBy.get();
		}

		public SimpleStringProperty createdByProperty() {
			return createdBy;
		}

		public String getCreatedTime() {
			return createdTime.get();
		}

		public SimpleStringProperty createdTimeProperty() {
			return createdTime;
		}

		public HBox getOperatingBox() {
			return operatingBox;
		}
	}

}
