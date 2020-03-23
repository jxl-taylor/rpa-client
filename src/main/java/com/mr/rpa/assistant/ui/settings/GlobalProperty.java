package com.mr.rpa.assistant.ui.settings;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.main.MainController;
import com.mr.rpa.assistant.ui.main.log.ILogShow;
import com.mr.rpa.assistant.ui.main.log.TaskHistoryController;
import com.mr.rpa.assistant.ui.main.statistic.StatisticController;
import com.mr.rpa.assistant.ui.main.task.TaskBeanController;
import de.schlichtherle.license.LicenseContent;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by feng on 2020/2/4 0004
 */
@Data
public class GlobalProperty {

	private static GlobalProperty globalProperty = new GlobalProperty();

	private GlobalProperty() {
	}

	public static GlobalProperty getInstance() {
		return globalProperty;
	}

	public final static String TITLE_PREFIX = "REC";
	public final static double DEFAULT_LOG_HEIGHT = 247.0;
	public final static double MAX_LOG_HEIGHT = 570.0;
	public final static double DEFAULT_LOG_LIST_HEIGHT = 195.0;
	public final static double MAX_LOG_LIST_HEIGHT = 519.0;
	public final static double SPLIT_POSITION_TASK_AND_LOG = 0.52;

	private LicenseContent licenseContent;
	private java.util.Date startDate;
	private SimpleStringProperty runningDuration = new SimpleStringProperty();

	private SimpleStringProperty title = new SimpleStringProperty("REC（未登录）");

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
	private MainController mainController;
	private StatisticController statisticController;
	private TaskBeanController taskBeanController;
	private MyInfoController myInfoController;
	private TaskHistoryController taskHistoryController;

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

	private MailServerInfo defaultMailServerInfo = new MailServerInfo("microrule.com", 25, "bot@microrule.com", "f0e5cbCj2", false);

	public void setTitle(String username) {
		this.title.set(String.format("REC（用户：%s）", username));
	}

	public List<JFXButton> getExitBtns() {

		if (exitBtns == null) {
			JFXButton confirmBtn = new JFXButton("确定");
			confirmBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				System.exit(0);
			});

			JFXButton cancelBtn = new JFXButton("取消");
			cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				StackPane rootPane = getRootPane();
				rootPane.getChildren().get(0).setEffect(null);
			});
			exitBtns = Lists.newArrayList(confirmBtn, cancelBtn);
		}
		return exitBtns;
	}

	public SysConfig getSysConfig() {
		return sysConfig;
	}

	public void refreshRunningDuration() {
		runningDuration.set(DateUtil.formatBetween(globalProperty.getStartDate(),
				new Date(),
				BetweenFormater.Level.MINUTE));
	}
}
