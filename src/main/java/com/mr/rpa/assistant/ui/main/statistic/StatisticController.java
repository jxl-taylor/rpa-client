package com.mr.rpa.assistant.ui.main.statistic;

import com.mr.rpa.assistant.database.DatabaseHandler;
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

	private DatabaseHandler databaseHandler;

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

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initGraphs();
		GlobalProperty.getInstance().setStatisticController(this);
	}

	private void initGraphs() {
		databaseHandler = DatabaseHandler.getInstance();

		totalTaskChart.setData(databaseHandler.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(databaseHandler.getTotalTaskLogGraphStatistics());
		taskChart.setData(databaseHandler.getTaskGraphStatistics(taskIDInput.getText()));
		enableDisableGraph(false);

	}

	public void refreshGraphs() {
		taskIDInput.setText("");
		enableDisableGraph(false);
		taskChart.setData(databaseHandler.getTaskGraphStatistics(taskIDInput.getText()));
		totalTaskChart.setData(databaseHandler.getTotalTaskGraphStatistics());
		totalTaskLogChart.setData(databaseHandler.getTotalTaskLogGraphStatistics());
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
		taskChart.setData(databaseHandler.getTaskGraphStatistics(taskIDInput.getText()));
		if (StringUtils.isNotBlank(taskIDInput.getText())) {
			enableDisableGraph(true);
		} else {
			enableDisableGraph(false);
		}
	}

}
