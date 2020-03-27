package com.mr.rpa.assistant.service;

import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by feng on 2020/3/26 0026
 */
public interface TaskLogService {

	List<TaskLogListController.TaskLog> loadUITaskLogList(String taskId);

	List<TaskLogListController.TaskLog> loadUITaskLogList(String taskId, Integer status);

	ObservableList<TaskLogListController.TaskLog> getUITaskLogList();

	void setMaxRow(int row);

	void insertNewTaskLog(TaskLog taskLog);

	TaskLog loadTaskLogById(String taskLogId);

	List<TaskLog> loadTaskLogByTaskId(String taskLogId);

	void deleteTaskLogByTaskId(String taskId);

	void deleteTaskLogByLogId(String taskLogId);

	void updateTaskLog(TaskLog taskLog);
}
