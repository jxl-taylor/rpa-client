package com.mr.rpa.assistant.database.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;

/**
 * Created by feng on 2020/2/21
 */
public class TaskDaoImpl implements TaskDao {

	private final static Logger LOGGER = Logger.getLogger(TaskDaoImpl.class);

	private DatabaseHandler handler = DatabaseHandler.getInstance();

	private static ObservableList<TaskListController.Task> taskList = FXCollections.observableArrayList();

	@Override
	public ObservableList<TaskListController.Task> getTaskList() {
		return taskList;
	}

	@Override
	public boolean deleteTask(TaskListController.Task task) {
		try {
			String deleteStatement = "DELETE FROM TASK WHERE ID = ?";
			PreparedStatement stmt = handler.getConnection().prepareStatement(deleteStatement);
			stmt.setString(1, task.getId());
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	@Override
	public boolean updateTask(TaskListController.Task task) {
		try {
			String update = "UPDATE TASK SET NAME=?, CRON=?, DESP=? ,PARAMS=? ,updateTime=? WHERE ID=?";
			PreparedStatement stmt = handler.getConnection().prepareStatement(update);
			int i = 1;
			stmt.setString(i++, task.getName());
			stmt.setString(i++, task.getCron());
			stmt.setString(i++, task.getDesp());
			stmt.setString(i++, task.getParams());
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setString(i++, task.getId());
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	/**
	 * update task running
	 *
	 * @param taskId
	 * @param running
	 * @return
	 */
	@Override
	public boolean updateTaskRunning(String taskId, boolean running) {
		try {
			String update = "UPDATE TASK SET RUNNING=?,updateTime=? WHERE ID=?";
			PreparedStatement stmt = handler.getConnection().prepareStatement(update);
			stmt.setBoolean(1, running);
			stmt.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
			stmt.setString(3, taskId);
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
			return false;
		}
	}

	@Override
	public boolean updateTaskStatus(String taskId, int status) {
		try {
			String update = "UPDATE TASK SET STATUS=?,updateTime=? WHERE ID=?";
			PreparedStatement stmt = handler.getConnection().prepareStatement(update);
			stmt.setInt(1, status);
			stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			stmt.setString(3, taskId);
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.error(ex);
			return false;
		}
	}

	@Override
	public ObservableList<PieChart.Data> getTotalTaskGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String runSqL = "SELECT COUNT(*) FROM TASK WHERE RUNNING = TRUE ";
			String stopSql = "SELECT COUNT(*) FROM TASK WHERE RUNNING = FALSE ";
			ResultSet rs = handler.execQuery(runSqL);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("已启动数 (" + count + ")", count));
			}
			rs.close();
			handler.closeStmt();
			rs = handler.execQuery(stopSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("未启动数 (" + count + ")", count));
			}
			rs.close();
			handler.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public ObservableList<PieChart.Data> getTotalTaskLogGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String succSqL = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 1 ";
			String failSql = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 2 ";
			ResultSet rs = handler.execQuery(succSqL);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("成功总次数 (" + count + ")", count));
			}
			rs.close();

			rs = handler.execQuery(failSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败总次数 (" + count + ")", count));
			}
			rs.close();
			handler.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public ObservableList<PieChart.Data> getTaskGraphStatistics(String taskId) {
		String sTaskId = StringUtils.isNotBlank(taskId) ? taskId : "undefined";
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String succSqL = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 1 AND TASK_id = '%s'";
			String failSql = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 2 AND TASK_id = '%s'";
			ResultSet rs = handler.execQuery(String.format(succSqL, sTaskId));
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("成功次数 (" + count + ")", count));
			}
			rs.close();
			handler.closeStmt();
			rs = handler.execQuery(String.format(failSql, sTaskId));
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败次数 (" + count + ")", count));
			}
			rs.close();
			handler.closeStmt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public boolean insertNewTask(Task task) {
		String sql = "INSERT INTO TASK(id,name,desp,params,nextTask,running,status,cron,createTime) VALUES(?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement statement = handler.getConnection().prepareStatement(sql);
			int i = 1;
			statement.setString(i++, task.getId());
			statement.setString(i++, task.getName());
			statement.setString(i++, task.getDesp());
			statement.setString(i++, task.getParams());
			statement.setString(i++, task.getNextTask());
			statement.setBoolean(i++, task.isRunning());
			statement.setInt(i++, task.getStatus());
			statement.setString(i++, task.getCron());
			statement.setTimestamp(i++, task.getCreateTime());
			statement.executeUpdate();
			statement.close();
			return true;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	@Override
	public List<TaskListController.Task> loadTaskList() {
		return loadTaskList(null, null);
	}

	@Override
	public List<TaskListController.Task> loadTaskList(String taskId, String taskName) {
		StringBuilder sql = new StringBuilder("SELECT * FROM TASK WHERE 1=1");
		if (StringUtils.isNotBlank(taskId)) {
			sql.append(String.format(" AND ID = '%s'", taskId));
		}

		if (StringUtils.isNotBlank(taskName)) {
			sql.append(String.format(" AND NAME = '%s'", taskName));
		}
		return loadTask(sql.toString());
	}

	private List<TaskListController.Task> loadTask(String sql) {
		taskList.clear();
		ResultSet rs = handler.execQuery(sql);
		if (rs == null) {
			return taskList;
		}
		try {
			while (rs.next()) {
				String id = rs.getString("id");
				String name = rs.getString("name");
				String cron = rs.getString("cron");
				String desp = rs.getString("desp");
				String params = rs.getString("params");
				String nextTask = rs.getString("nextTask");
				Boolean running = rs.getBoolean("running");
				Integer status = rs.getInt("status");

				//TODO count
				taskList.add(new TaskListController.Task(id, name, desp, params, nextTask, running, status, cron, 0, 0));

			}
			handler.closeStmt();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		}
		return taskList;
	}

	@Override
	public Task queryTaskById(String id) {
		String sql = String.format("SELECT * FROM TASK WHERE ID = '%s'", id);
		List<Task> taskList = queryTask(sql);
		return CollectionUtil.isEmpty(taskList) ? null : taskList.get(0);
	}

	@Override
	public Task queryTaskByName(String name) {
		String sql = String.format("SELECT * FROM TASK WHERE NAME = '%s'", name);
		List<Task> taskList = queryTask(sql);
		return CollectionUtil.isEmpty(taskList) ? null : taskList.get(0);
	}

	@Override
	public List<Task> queryTaskByNextTask(String nextTask) {
		String sql = String.format("SELECT * FROM TASK WHERE NEXTTASK = '%s'", nextTask);
		return queryTask(sql);
	}

	@Override
	public List<Task> queryTaskList() {
		String sql = "SELECT * FROM TASK ";
		return queryTask(sql);
	}

	private List<Task> queryTask(String sql) {
		List<Task> taskList = Lists.newArrayList();
		ResultSet rs = handler.execQuery(sql);
		try {
			while (rs.next()) {
				Task task = new Task(rs.getString("id"),
						rs.getString("name"),
						rs.getString("desp"),
						rs.getString("params"),
						rs.getString("nextTask"),
						rs.getBoolean("running"),
						rs.getInt("status"),
						rs.getString("cron"),
						0,
						0,
						rs.getTimestamp("createTime"),
						rs.getTimestamp("updateTime"));

				taskList.add(task);
			}
			handler.closeStmt();
		} catch (SQLException ex) {
			LOGGER.error(ex);
		}
		return taskList;
	}

	@Override
	public void removeTask(TaskListController.Task selectedForDeletion) {
		taskList.remove(selectedForDeletion);
	}

}
