package com.mr.rpa.assistant.database;

import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by feng on 2020/2/21
 */
public interface TaskLogDao {

	 List<TaskLogListController.TaskLog> loadTaskLogList(String taskId);

	 List<TaskLogListController.TaskLog> loadTaskLogList(String taskId, Integer status);

	ObservableList<TaskLogListController.TaskLog> getTaskLogList();

	void setMaxRow(int row);

	boolean insertNewTaskLog(TaskLog taskLog);

	List<TaskLogListController.TaskLog> loadLogTask(String sql);

	TaskLog loadTaskLogById(String taskLogId);

	List<TaskLog> loadTaskLogByTaskId(String taskLogId);

	boolean deleteTaskLogByTaskId(String taskId);

	boolean deleteTaskLogByLogId(String taskLogId);

	boolean updateTaskLog(TaskLog taskLog);
}
