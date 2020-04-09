package com.mr.rpa.assistant.ui.listtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.SysConfig;
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

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

@Log4j
public class BotMarketListController implements Initializable {
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

					});
		} catch (Throwable e) {
			log.error(e);
			AlertMaker.showErrorMessage("BOT市场", e.getMessage());
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
			this.operatingBox = new HBox(downloadLink, updateLink);
			operatingBox.setSpacing(20);
			operatingBox.setAlignment(Pos.CENTER);
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
