package com.mr.rpa.assistant.database.impl;

import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskLogDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 2020/2/21
 */
@Log4j
@Repository
public class TaskLogDaoImpl implements TaskLogDao {

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Resource
	private DatabaseHandler handler;

	private static ObservableList<TaskLogListController.TaskLog> taskLogList = FXCollections.observableArrayList();

	@Override
	public ObservableList<TaskLogListController.TaskLog> getTaskLogList() {
		return taskLogList;
	}

	private static int maxRow = 100;

	@Override
	public void setMaxRow(int row) {
		maxRow = row;
	}

	@Override
	public boolean insertNewTaskLog(TaskLog taskLog) {
		PreparedStatement statement = null;
		try {
			statement = handler.getConnection().prepareStatement(
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
			log.error(ex);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public List<TaskLogListController.TaskLog> loadTaskLogList(String taskId) {
		return loadTaskLogList(taskId, null);
	}

	@Override
	public List<TaskLogListController.TaskLog> loadTaskLogList(String taskId, Integer status) {
		StringBuilder sql = new StringBuilder("SELECT * FROM TASK_LOG WHERE 1=1 ");
		sql.append(String.format(" AND TASK_ID = '%s'", taskId));

		if (status != null) {
			sql.append(String.format(" AND STATUS = %d", status));
		}
		sql.append(String.format(" ORDER BY STARTTIME DESC OFFSET 0 ROWS FETCH NEXT %d ROWS ONLY", maxRow));
		return loadLogTask(sql.toString());
	}

	@Override
	public List<TaskLogListController.TaskLog> loadLogTask(String sql) {
		taskLogList.clear();
		Map<String, Object> params = new HashMap<String, Object>();

		taskLogList.addAll(namedParameterJdbcTemplate.query(sql, params, new RowMapper<TaskLogListController.TaskLog>() {
			@Override
			public TaskLogListController.TaskLog mapRow(ResultSet rs, int i) throws SQLException {
				String seq = String.valueOf(i++);
				String id = rs.getString("id");
				String taskId = rs.getString("task_id");
				Integer status = rs.getInt("status");
				String error = rs.getString("error");
				java.util.Date startTime = rs.getTimestamp("startTime");
				java.util.Date endTime = rs.getTimestamp("endTime");
				return new TaskLogListController.TaskLog(seq, id, taskId, status, error, startTime, endTime);
			}
		}));
		return taskLogList;
	}

	@Override
	public TaskLog loadTaskLogById(String taskLogId) {
		String sql = String.format("SELECT * FROM TASK_LOG WHERE ID = '%s'", taskLogId);
		TaskLog taskLog = null;
		ResultSet rs = handler.execQuery(sql);
		if (rs == null) return null;
		try {
			int i = 1;
			if (rs.next()) {
				taskLog = new TaskLog();
				taskLog.setId(rs.getString("id"));
				taskLog.setTaskId(rs.getString("task_id"));
				taskLog.setStatus(rs.getInt("status"));
				taskLog.setError(rs.getString("error"));
				taskLog.setStartTime(rs.getTimestamp("startTime"));
				taskLog.setEndTime(rs.getTimestamp("endTime"));
			}
		} catch (SQLException ex) {
			log.error(ex);
		} finally {
			try {
				rs.close();
				handler.closeStmt();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return taskLog;
	}

	@Override
	public boolean deleteTaskLogByLogId(String taskLogId) {
		PreparedStatement stmt = null;
		try {
			String deleteStatement = "DELETE FROM TASK_LOG WHERE ID = ?";
			stmt = handler.getConnection().prepareStatement(deleteStatement);
			stmt.setString(1, taskLogId);
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			log.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public boolean deleteTaskLogByTaskId(String taskId) {
		PreparedStatement stmt = null;
		try {
			String deleteStatement = "DELETE FROM TASK_LOG WHERE TASK_ID = ?";
			stmt = handler.getConnection().prepareStatement(deleteStatement);
			stmt.setString(1, taskId);
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			log.error(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public boolean updateTaskLog(TaskLog taskLog) {
		PreparedStatement stmt = null;
		try {

			String update = "UPDATE TASK_LOG SET STATUS=?, ERROR = ?, endTime=? WHERE ID=?";

			stmt = handler.getConnection().prepareStatement(update);
			stmt.setInt(1, taskLog.getStatus());
			stmt.setString(2, taskLog.getError());
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			stmt.setString(4, taskLog.getId());
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("STATUS", taskLog.getStatus());
			params.put("ERROR", taskLog.getError());
			params.put("endTime", new Timestamp(System.currentTimeMillis()));
			params.put("ID", taskLog.getId());
			namedParameterJdbcTemplate.update(update, params);
		} catch (SQLException ex) {
			log.error(ex);
			return false;
		}
		return true;
	}

}
