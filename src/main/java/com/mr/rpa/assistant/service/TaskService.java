package com.mr.rpa.assistant.service;

import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

import java.util.List;

/**
 * Created by feng on 2020/3/26 0026
 */
public interface TaskService {
	ObservableList<TaskListController.Task> getUITaskList();

	void deleteTask(String taskId);

	void updateTask(Task task);

	void updateUITask(TaskListController.Task task);

	void updateTaskRunning(String taskId, boolean running);

	void updateTaskStatus(String taskId, int status);

	ObservableList<PieChart.Data> getTotalTaskGraphStatistics();

	ObservableList<PieChart.Data> getTotalTaskLogGraphStatistics();

	ObservableList<PieChart.Data> getTaskGraphStatistics(String taskId);

	void insertNewTask(Task task);

	List<TaskListController.Task> loadUITaskList();

	List<TaskListController.Task> loadUITaskList(String taskId, String taskName);

	Task queryTaskByName(String name);

	List<Task> queryTaskByNextTask(String nextTask);

	List<Task> queryTaskList();

	Task queryTaskById(String id);

	void removeTask(TaskListController.Task selectedForDeletion);
}
