package com.mr.rpa.assistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mr.rpa.assistant.dao.TaskLogMapper;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.service.TaskLogService;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by feng on 2020/3/26 0026
 */
public class TaskLogServiceImpl implements TaskLogService {

	private static ObservableList<TaskLogListController.TaskLog> taskLogList = FXCollections.observableArrayList();

	private static int maxRow = 100;

	private TaskLogMapper taskLogMapper;

	public TaskLogServiceImpl(SqlSession session) {
		taskLogMapper = session.getMapper(TaskLogMapper.class);
	}

	@Override
	public List<TaskLogListController.TaskLog> loadUITaskLogList(String taskId) {
		return loadUITaskLogList(taskId, null);
	}

	@Override
	public List<TaskLogListController.TaskLog> loadUITaskLogList(String taskId, Integer status) {
		taskLogList.clear();
		AtomicInteger seq = new AtomicInteger(1);
		taskLogMapper.queryTaskLogList(maxRow, null, taskId, status).forEach(item ->
				taskLogList.add(new TaskLogListController.TaskLog(seq.getAndIncrement(), item.getId(), item.getTaskId(),
						item.getStatus(), item.getError(), item.getStartTime(), item.getEndTime())));
		return taskLogList;
	}

	@Override
	public ObservableList<TaskLogListController.TaskLog> getUITaskLogList() {
		return taskLogList;
	}

	@Override
	public void setMaxRow(int row) {
		maxRow = row;
	}

	@Override
	public void insertNewTaskLog(TaskLog taskLog) {
		taskLogMapper.insertNewTaskLog(taskLog);
	}

	@Override
	public TaskLog loadTaskLogById(String taskLogId) {
		List<TaskLog> taskLogList = taskLogMapper.queryTaskLogList(maxRow, taskLogId, null, null);
		return CollectionUtil.isEmpty(taskLogList) ? null : taskLogList.get(0);
	}

	@Override
	public List<TaskLog> loadTaskLogByTaskId(String taskId) {
		return taskLogMapper.queryTaskLogList(maxRow, null, taskId, null);
	}

	@Override
	public void deleteTaskLogByTaskId(String taskId) {
		taskLogMapper.deleteTaskLog(null, taskId);
	}

	@Override
	public void deleteTaskLogByLogId(String taskLogId) {
		taskLogMapper.deleteTaskLog(taskLogId, null);
	}

	@Override
	public void updateTaskLog(TaskLog taskLog) {
		taskLogMapper.updateTaskLog(taskLog);
	}
}
