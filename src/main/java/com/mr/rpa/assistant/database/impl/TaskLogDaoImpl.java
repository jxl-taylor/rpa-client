package com.mr.rpa.assistant.database.impl;

import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by feng on 2020/2/21
 */
public class TaskLogDaoImpl implements TaskLogDao {

	private final static Logger LOGGER = Logger.getLogger(TaskLogDaoImpl.class);

	DatabaseHandler handler = DatabaseHandler.getInstance();
	Connection connection = DatabaseHandler.getInstance().getConnection();

	private static ObservableList<TaskLogListController.TaskLog> taskLogList = FXCollections.observableArrayList();

	@Override
	public ObservableList<TaskLogListController.TaskLog> getTaskLogList() {
		return taskLogList;
	}

	@Override
	public  boolean insertNewTaskLog(TaskLog taskLog) {
		try {
			PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO TASK_LOG(id, task_id, status, error, startTime, endTime) VALUES(?,?,?,?,?,?)");
			int i = 1;
			statement.setString(i++, taskLog.getId());
			statement.setString(i++, taskLog.getTaskId());
			statement.setInt(i++, taskLog.getStatus());
			statement.setString(i++, taskLog.getError());
			statement.setTimestamp(i++, taskLog.getStartTime());
			statement.setTimestamp(i++, taskLog.getEndTime());
			return statement.executeUpdate() > 0;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	@Override
	public  List<TaskLogListController.TaskLog> loadTaskLogList(String taskId) {
		return loadTaskLogList(taskId, null);
	}

	@Override
	public  List<TaskLogListController.TaskLog> loadTaskLogList(String taskId, Integer status) {
		StringBuilder sql = new StringBuilder("SELECT * FROM TASK_LOG WHERE 1=1");
		sql.append(String.format(" AND TASK_ID = '%s'", taskId));

		if (status != null) {
			sql.append(String.format(" AND STATUS = %d", status));
		}
		return loadLogTask(sql.toString());
	}

	@Override
	public List<TaskLogListController.TaskLog> loadLogTask(String sql) {
		taskLogList.clear();
		ResultSet rs = handler.execQuery(sql);
		try {
			int i = 1;
			while (rs.next()) {
				Integer seq = i++;
				String id = rs.getString("id");
				String taskId = rs.getString("task_id");
				Integer status = rs.getInt("status");
				String error = rs.getString("error");
				java.util.Date startTime = rs.getTimestamp("startTime");
				java.util.Date endTime = rs.getTimestamp("endTime");

				taskLogList.add(new TaskLogListController.TaskLog(seq, taskId, status, error, startTime, endTime));

			}
			handler.closeStmt();
		} catch (SQLException ex) {
			java.util.logging.Logger.getLogger(TaskListController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		return taskLogList;
	}

}
