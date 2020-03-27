package com.mr.rpa.assistant.ui.main.statistic;

import com.jfoenix.controls.JFXComboBox;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.ui.settings.ServiceFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by feng on 2020/2/21
 */
public class StatisticController implements Initializable {

	@FXML
	private VBox rootPane;

	@FXML
	private PieChart totalTaskChart;

	@FXML
	private PieChart totalTaskLogChart;
	@FXML
	private PieChart taskChart;

	@FXML
	private JFXComboBox<String> taskNameCbx;

	private TaskService taskService = ServiceFactory.getService(TaskService.class);

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initGraphs();
		GlobalProperty.getInstance().setStatisticController(this);

	}

	private void initGraphs() {
		totalTaskChart.setData(taskService.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(taskService.getTotalTaskLogGraphStatistics());
		setTaskNameCbx();
		enableDisableGraph(false);

	}

	public void refreshGraphs() {
		enableDisableGraph(false);
		setTaskNameCbx();
		totalTaskChart.setData(taskService.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(taskService.getTotalTaskLogGraphStatistics());
	}

	private void enableDisableGraph(Boolean status) {
		if (status) {
			taskChart.setOpacity(1);
		} else {
			taskChart.setOpacity(0);
		}
	}

	@FXML
	private void loadTaskStatistic(ActionEvent event) {
		taskChart.setData(taskService.getTaskGraphStatistics(taskNameCbx.getValue()));
		if (StringUtils.isNotBlank(taskNameCbx.getValue())) {
			enableDisableGraph(true);
		} else {
			enableDisableGraph(false);
		}
	}

	private void setTaskNameCbx() {
		taskNameCbx.getItems().clear();
		taskService.queryTaskList().forEach(task -> taskNameCbx.getItems().add(task.getName()));
	}
}
