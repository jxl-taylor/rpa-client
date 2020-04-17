package com.mr.rpa.assistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mr.rpa.assistant.dao.TaskMapper;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.User;
import com.mr.rpa.assistant.service.TaskService;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import com.mr.rpa.assistant.util.SystemContants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.apache.ibatis.session.SqlSession;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 2020/3/26 0026
 */
public class TaskServiceImpl implements TaskService {

	private TaskMapper taskMapper;

	public TaskServiceImpl(SqlSession session) {
		taskMapper = session.getMapper(TaskMapper.class);
	}

	private static ObservableList<TaskListController.Task> taskList = FXCollections.observableArrayList();

	@Override
	public ObservableList<TaskListController.Task> getUITaskList() {
		return taskList;
	}

	@Override
	public void deleteTask(String taskId) {
		taskMapper.deleteTask(taskId);
	}

	@Override
	public void updateTask(Task task) {
		User user = GlobalProperty.getInstance().getCurrentUser();
		task.setUpdateBy(user.getUsername());
		task.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		taskMapper.updateTask(task);
	}

	@Override
	public void updateUITask(TaskListController.Task task) {
		updateTask(queryTaskById(task.getId()));
	}

	@Override
	public void updateTaskRunning(String taskId, boolean running) {
		taskMapper.updateTaskRunning(taskId, new Timestamp(System.currentTimeMillis()), running);
	}

	@Override
	public void updateTaskStatus(String taskId, int status) {
		taskMapper.updateTaskStatus(taskId, new Timestamp(System.currentTimeMillis()), status);
	}

	@Override
	public ObservableList<PieChart.Data> getTotalTaskGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		int runningCount = taskMapper.getTotalTaskCount(true);
		int unRunningCount = taskMapper.getTotalTaskCount(false);
		data.add(new PieChart.Data("已启动数 (" + runningCount + ")", runningCount));
		data.add(new PieChart.Data("未启动数 (" + unRunningCount + ")", unRunningCount));
		return data;
	}

	@Override
	public ObservableList<PieChart.Data> getTotalTaskLogGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		int succCount = taskMapper.getTotalTaskLogCount(null, SystemContants.TASK_LOG_STATUS_SUCCESS);
		int failCount = taskMapper.getTotalTaskLogCount(null, SystemContants.TASK_LOG_STATUS_FAIL);
		data.add(new PieChart.Data("成功次数 (" + succCount + ")", succCount));
		data.add(new PieChart.Data("失败次数 (" + failCount + ")", failCount));
		return data;
	}

	@Override
	public ObservableList<PieChart.Data> getTaskGraphStatistics(String taskName) {
		Task task = queryTaskByName(taskName);
		String sTaskId = task != null ? task.getId() : "undefined";
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		int succCount = taskMapper.getTotalTaskLogCount(sTaskId, SystemContants.TASK_LOG_STATUS_SUCCESS);
		int failCount = taskMapper.getTotalTaskLogCount(sTaskId, SystemContants.TASK_LOG_STATUS_FAIL);
		data.add(new PieChart.Data("成功次数 (" + succCount + ")", succCount));
		data.add(new PieChart.Data("失败次数 (" + failCount + ")", failCount));
		return data;
	}

	@Override
	public void insertNewTask(Task task) {
		taskMapper.insertNewTask(task);
	}

	@Override
	public List<TaskListController.Task> loadUITaskList() {
		return loadUITaskList(null, null);
	}

	@Override
	public List<TaskListController.Task> loadUITaskList(String taskId, String taskName) {
		taskList.clear();
		AtomicInteger seq = new AtomicInteger(1);
		taskMapper.queryTaskList(taskId, taskName, null).forEach(item ->
				taskList.add(new TaskListController.Task(seq.getAndIncrement(), item.getId(), item.getName(), item.getMainTask(),
						item.getDesp(), item.getParams(), item.getNextTask(),
						item.isRunning(), item.getStatus(), item.getCron(),
						taskMapper.getTotalTaskLogCount(item.getId(), SystemContants.TASK_LOG_STATUS_SUCCESS),
						taskMapper.getTotalTaskLogCount(item.getId(), SystemContants.TASK_LOG_STATUS_FAIL),
						item.getCreateBy())
				));
		return taskList;
	}

	@Override
	public Task queryTaskByName(String name) {
		List<Task> taskList = taskMapper.queryTaskList(null, name, null);
		return CollectionUtil.isEmpty(taskList) ? null : taskList.get(0);
	}

	@Override
	public List<Task> queryTaskByNextTask(String nextTask) {
		return taskMapper.queryTaskList(null, null, nextTask);
	}

	@Override
	public List<Task> queryTaskList() {
		return taskMapper.queryTaskList(null, null, null);
	}

	@Override
	public Task queryTaskById(String id) {
		List<Task> taskList = taskMapper.queryTaskList(id, null, null);
		return CollectionUtil.isEmpty(taskList) ? null : taskList.get(0);
	}

	@Override
	public void removeTask(TaskListController.Task selectedForDeletion) {
		taskList.remove(selectedForDeletion);
	}
}
