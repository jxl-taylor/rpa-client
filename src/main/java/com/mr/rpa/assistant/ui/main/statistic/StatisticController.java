package com.mr.rpa.assistant.ui.main.statistic;

import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TextField;
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
	private TextField taskIDInput;

	private TaskDao taskDao = DatabaseHandler.getInstance().getTaskDao();

	private TaskLogDao taskLogDao = DatabaseHandler.getInstance().getTaskLogDao();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initGraphs();
		GlobalProperty.getInstance().setStatisticController(this);
	}

	private void initGraphs() {
		totalTaskChart.setData(taskDao.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(taskDao.getTotalTaskLogGraphStatistics());
		taskChart.setData(taskDao.getTaskGraphStatistics(taskIDInput.getText()));
		enableDisableGraph(false);

	}

	public void refreshGraphs() {
		taskIDInput.setText("");
		enableDisableGraph(false);
		taskChart.setData(taskDao.getTaskGraphStatistics(taskIDInput.getText()));
		totalTaskChart.setData(taskDao.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(taskDao.getTotalTaskLogGraphStatistics());
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
		taskChart.setData(taskDao.getTaskGraphStatistics(taskIDInput.getText()));
		if (StringUtils.isNotBlank(taskIDInput.getText())) {
			enableDisableGraph(true);
		} else {
			enableDisableGraph(false);
		}
	}

}
