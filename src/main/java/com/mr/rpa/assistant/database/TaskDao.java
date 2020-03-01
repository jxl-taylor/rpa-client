package com.mr.rpa.assistant.database;

import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

import java.util.List;

/**
 * Created by feng on 2020/2/21
 */
public interface TaskDao {

	ObservableList<TaskListController.Task> getTaskList();

	boolean deleteTask(TaskListController.Task task);

	boolean updateTask(TaskListController.Task task);

	boolean updateTaskRunning(String taskId, boolean running);

	boolean updateTaskStatus(String taskId, int status);

	ObservableList<PieChart.Data> getTotalTaskGraphStatistics();

	ObservableList<PieChart.Data> getTotalTaskLogGraphStatistics();

	ObservableList<PieChart.Data> getTaskGraphStatistics(String taskId);

	boolean insertNewTask(Task task);

	List<TaskListController.Task> loadTaskList();

	List<TaskListController.Task> loadTaskList(String taskId, String taskName);

	Task queryTaskByName(String name);

	List<Task> queryTaskByNextTask(String nextTask);

	List<Task> queryTaskList();

	Task queryTaskById(String id);

	void removeTask(TaskListController.Task selectedForDeletion);
}
