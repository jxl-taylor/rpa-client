package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.service.SysConfigService;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.login.LoginController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.main.log.ILogShow;
import com.mr.rpa.assistant.ui.main.log.TaskHistoryController;
import com.mr.rpa.assistant.ui.main.statistic.StatisticController;
import com.mr.rpa.assistant.ui.main.task.TaskBeanController;
import com.mr.rpa.assistant.ui.main.toolbar.ToolbarController;
import com.mr.rpa.assistant.util.AssistantUtil;
import com.mr.rpa.assistant.util.CommonUtil;
import com.mr.rpa.assistant.util.Pair;
import com.mr.rpa.assistant.util.SystemContants;
import de.schlichtherle.license.LicenseContent;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.quartz.JobDataMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by feng on 2020/2/4 0004
 */
@Data
@Log4j
public class GlobalProperty {

	private static GlobalProperty globalProperty = new GlobalProperty();

	private GlobalProperty() {
	}

	public static GlobalProperty getInstance() {
		return globalProperty;
	}

	private boolean debug;

	public void initDB() {
		Connection connection = globalProperty.createConnection();
		if (connection == null) throw new RuntimeException("数据库初始化错误");
		ServiceFactory.init(globalProperty.session);
		globalProperty.inflateDB(connection);
		globalProperty.initSysConfig(ServiceFactory.getService(SysConfigService.class));
	}

	public final static String TITLE_PREFIX = "REC";
	public final static double DEFAULT_LOG_HEIGHT = 247.0;
	public final static double MAX_LOG_HEIGHT = 570.0;
	public final static double DEFAULT_LOG_LIST_HEIGHT = 195.0;
	public final static double MAX_LOG_LIST_HEIGHT = 519.0;
	public final static double SPLIT_POSITION_TASK_AND_LOG = 0.52;

	private SqlSession session;

	private LicenseContent licenseContent;
	private java.util.Date startDate;
	private SimpleStringProperty runningDuration = new SimpleStringProperty();

	private SimpleStringProperty title = new SimpleStringProperty("REC（未登录）");

	private LinkedBlockingQueue<Pair<String, JobDataMap>> taskQueue = new LinkedBlockingQueue<>(100);

	private User currentUser;

	//右角菜单显示控制
	private SimpleBooleanProperty taskPaneVisible = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty settingPaneVisible = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty myInfoPaneVisible = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty statisticPaneVisible = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty taskHistoryPaneVisible = new SimpleBooleanProperty(false);
	private SimpleBooleanProperty taskLogPaneVisible = new SimpleBooleanProperty(false);

	/**
	 * controller
	 */
	private LoginController loginController;
	private MainController mainController;
	private StatisticController statisticController;
	private TaskBeanController taskBeanController;
	private MyInfoController myInfoController;
	private TaskHistoryController taskHistoryController;
	private TaskListController taskListController;
	private ToolbarController toolbarController;

	private List<ILogShow> logShows = Lists.newArrayList();

	private StackPane rootPane;
	private List<JFXButton> exitBtns;

	//selectd Task
	private SimpleStringProperty selectedTaskId = new SimpleStringProperty();
	//selectd Task Log Id
	private SimpleStringProperty selectedTaskLogId = new SimpleStringProperty();
	//All Task Log
	private SimpleStringProperty allLog = new SimpleStringProperty();

	//selectd Task Log height
	private SimpleDoubleProperty logAreaMinHeight = new SimpleDoubleProperty(DEFAULT_LOG_HEIGHT);
	//selectd Task Log List height
	private SimpleDoubleProperty logListHeight = new SimpleDoubleProperty();
	//Log Text
	private LogTextCollector logTextCollector = new LogTextCollector();

	//log text
	private JFXTextArea logTextArea;
	//task tabel list
	private TableView<TaskListController.Task> taskTableView;

	private SysConfig sysConfig = new SysConfig();

	private MailServerInfo defaultMailServerInfo = new MailServerInfo(SystemContants.DEFAULT_ADMIN_MAIL_SERVER,
			SystemContants.DEFAULT_ADMIN_MAIL_PORT,
			SystemContants.DEFAULT_ADMIN_MAIL_USERNAME,
			SystemContants.DEFAULT_ADMIN_MAIL_PASSWORD,
			false);

	public void setTitle(String username) {
		this.title.set(String.format("REC(用户:%s)", username));
	}

	public List<JFXButton> getExitBtns(String username) {

		if (exitBtns == null) {
			JFXButton confirmBtn = new JFXButton("退出");
			confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				System.exit(0);
			});

			JFXButton logoutBtn = new JFXButton("注销");
			logoutBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				AlertMaker.showMaterialDialog(getRootPane(),
						getRootPane().getChildren().get(0),
						loginController.getRootPane(), "登录", "", false);
				globalProperty.getLogShows().forEach(ILogShow::refreshLog);
			});

			JFXButton cancelBtn = new JFXButton("取消");
			cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				StackPane rootPane = getRootPane();
				rootPane.getChildren().get(0).setEffect(null);
			});
			exitBtns = Lists.newArrayList(confirmBtn, logoutBtn, cancelBtn);
		}
		if (StringUtils.isNotEmpty(username) && !CommonUtil.isAdmin(username) && exitBtns.size() > 2)
			exitBtns.remove(0);
		return exitBtns;
	}

	public SysConfig getSysConfig() {
		return sysConfig;
	}

	public void refreshRunningDuration() {
		runningDuration.set(DateUtil.formatBetween(globalProperty.getStartDate(),
				new Date(),
				BetweenFormater.Level.MINUTE));
		Platform.runLater(() -> {
			globalProperty.getMyInfoController().refreshExpireTime();
			int licExpireDays = getLicExpireDays();
			if (licExpireDays < 31) {
				this.title.set(String.format("REC(用户:%s) 距离到期日还有%s天",
						globalProperty.getCurrentUser().getNick(), licExpireDays));
			} else {
				this.title.set(String.format("REC(用户:%s) ", globalProperty.getCurrentUser().getNick()));
			}
		});

	}

	public int getLicExpireDays() {
		if (licenseContent == null) return 0;
		String dayString = DateUtil.formatBetween(licenseContent.getNotAfter(),
				new Date(),
				BetweenFormater.Level.DAY);
		return Integer.parseInt(dayString.replace("天", ""));
	}

	private void inflateDB(Connection conn) {
		List<String> tableData = new ArrayList<>();
		try {
			Set<String> loadedTables = getDBTables(conn);
			log.info("准备装载数据库表: " + loadedTables);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(GlobalProperty.class.getClass().getResourceAsStream("/database/tables.xml"));
			NodeList nList = doc.getElementsByTagName("table-entry");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				Element entry = (Element) nNode;
				String tableName = entry.getAttribute("name");
				String query = entry.getAttribute("col-data");
				if (!loadedTables.contains(tableName.toLowerCase())) {
					String createSQL = String.format("CREATE TABLE %s (%s)", tableName, query);
					tableData.add(createSQL);
					log.info(String.format("创建表成功，SQL:[%s]", createSQL));
				}
			}
			if (tableData.isEmpty()) {
				log.info("数据库表已经载入");
			} else {
				log.warn("开始创建表结构.");
				createTables(tableData, conn);
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	private Connection createConnection() {
		Reader reader;
		try {
			String resource = "MyBatisConfig.xml";
			reader = Resources.getResourceAsReader(resource);
			SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder()
					.build(reader);
			session = sqlMapper.openSession(true);
			return session.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "无法载入数据库," + Arrays.toString(e.getStackTrace()), "数据库错误:" + e.getMessage(), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return null;
	}

	private Set<String> getDBTables(Connection conn) throws SQLException {
		Set<String> set = new HashSet<>();
		DatabaseMetaData dbmeta = conn.getMetaData();
		readDBTable(set, dbmeta, "TABLE", null);
		return set;
	}

	private void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema) throws SQLException {
		ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{searchCriteria});
		while (rs.next()) {
			set.add(rs.getString("TABLE_NAME").toLowerCase());
		}
		rs.close();
	}

	private void createTables(List<String> tableData, Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		statement.closeOnCompletion();
		for (String command : tableData) {
			log.info(command);
			statement.addBatch(command);
		}
		statement.executeBatch();
	}

	public void initSysConfig(SysConfigService sysConfigService) {
		SysConfig dbSysConfig = sysConfigService.query();
		if (dbSysConfig != null) {
			globalProperty.setSysConfig(dbSysConfig);
		} else {
			sysConfigService.insert();
		}
	}
}
