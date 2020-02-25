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

	boolean insertNewTaskLog(TaskLog taskLog);

	List<TaskLogListController.TaskLog> loadLogTask(String sql);

	TaskLog loadTaskLogById(String taskLogId);

	boolean deleteTaskLog(String taskId);

	boolean updateTaskLog(TaskLog taskLog);
}
