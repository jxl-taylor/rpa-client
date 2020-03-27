package com.mr.rpa.assistant.service;

import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

import java.util.List;

/**
 * Created by feng on 2020/3/26 0026
 */
public interface SysConfigService {

	SysConfig query();

	void insert();

	void update();
}
