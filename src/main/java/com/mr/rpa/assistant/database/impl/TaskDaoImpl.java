package com.mr.rpa.assistant.database.impl;

import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.database.TaskDao;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by feng on 2020/2/21
 */
public class TaskDaoImpl implements TaskDao {

	private final static Logger LOGGER = Logger.getLogger(TaskDaoImpl.class);

	DatabaseHandler handler = DatabaseHandler.getInstance();

	Connection connection = DatabaseHandler.getInstance().getConnection();

	private static ObservableList<TaskListController.Task> taskList = FXCollections.observableArrayList();

	@Override
	public ObservableList<TaskListController.Task> getTaskList() {
		return taskList;
	}

	@Override
	public boolean deleteTask(TaskListController.Task task) {
		try {
			String deleteStatement = "DELETE FROM TASK WHERE ID = ?";
			PreparedStatement stmt = connection.prepareStatement(deleteStatement);
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
			String update = "UPDATE TASK SET NAME=?, DESP=? WHERE ID=?";
			PreparedStatement stmt = connection.prepareStatement(update);
			stmt.setString(1, task.getName());
			stmt.setString(2, task.getDesp());
			stmt.setString(3, task.getId());
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
			String update = "UPDATE TASK SET RUNNING=? WHERE ID=?";
			PreparedStatement stmt = connection.prepareStatement(update);
			stmt.setBoolean(1, running);
			stmt.setString(2, taskId);
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
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
				data.add(new PieChart.Data("已启动数 (" + count + ")", 100));
			}
			rs.close();
			handler.closeStmt();
			rs = handler.execQuery(stopSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("未启动数 (" + count + ")", 50));
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
				data.add(new PieChart.Data("成功总次数 (" + count + ")", 100));
			}
			rs.close();

			rs = handler.execQuery(failSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败总次数 (" + count + ")", 50));
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
				data.add(new PieChart.Data("成功次数 (" + count + ")", 100));
			}
			rs.close();
			handler.closeStmt();
			rs = handler.execQuery(String.format(failSql, sTaskId));
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败次数 (" + count + ")", 50));
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
		try {
			PreparedStatement statement = connection.prepareStatement(
					"INSERT INTO TASK(id,name,desp,running,status) VALUES(?,?,?,?,?)");
			int i = 1;
			statement.setString(i++, task.getId());
			statement.setString(i++, task.getName());
			statement.setString(i++, task.getDesp());
			statement.setBoolean(i++, task.getRunning());
			statement.setInt(i++, task.getStatus());
			return statement.executeUpdate() > 0;
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
	public  List<TaskListController.Task> loadTaskList(String taskId, String taskName) {
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
		try {
			while (rs.next()) {
				String id = rs.getString("id");
				String name = rs.getString("name");
				String desp = rs.getString("desp");
				Boolean running = rs.getBoolean("running");
				Integer status = rs.getInt("status");

				//TODO count
				taskList.add(new TaskListController.Task(id, name, desp, running, status, 0, 0));

			}
			handler.closeStmt();
		} catch (SQLException ex) {
			java.util.logging.Logger.getLogger(TaskListController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		return taskList;
	}

	@Override
	public void removeTask(TaskListController.Task selectedForDeletion) {
		taskList.remove(selectedForDeletion);
	}

}
