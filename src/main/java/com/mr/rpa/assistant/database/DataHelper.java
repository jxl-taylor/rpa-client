package com.mr.rpa.assistant.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.mr.rpa.assistant.data.model.Book;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import com.mr.rpa.assistant.data.model.Task;
import com.mr.rpa.assistant.data.model.TaskLog;
import com.mr.rpa.assistant.ui.listmember.MemberListController;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.main.log.TaskLogListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author afsal
 */
public class DataHelper {

	private final static Logger LOGGER = Logger.getLogger(DatabaseHandler.class);

	private static ObservableList<TaskListController.Task> taskList = FXCollections.observableArrayList();
	private static ObservableList<TaskLogListController.TaskLog> taskLogList = FXCollections.observableArrayList();

	public static ObservableList<TaskListController.Task> getTaskList() {
		return taskList;
	}

	public static ObservableList<TaskLogListController.TaskLog> getTaskLogList() {
		return taskLogList;
	}

	public static boolean insertNewBook(Book book) {
		try {
			PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
					"INSERT INTO BOOK(id,title,author,publisher,isAvail) VALUES(?,?,?,?,?)");
			statement.setString(1, book.getId());
			statement.setString(2, book.getTitle());
			statement.setString(3, book.getAuthor());
			statement.setString(4, book.getPublisher());
			statement.setBoolean(5, book.getAvailability());
			return statement.executeUpdate() > 0;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public static boolean insertNewTask(Task task) {
		try {
			PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
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

	public static boolean insertNewTaskLog(TaskLog taskLog) {
		try {
			PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
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

	public static boolean insertNewMember(MemberListController.Member member) {
		try {
			PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
					"INSERT INTO MEMBER(id,name,mobile,email) VALUES(?,?,?,?)");
			statement.setString(1, member.getId());
			statement.setString(2, member.getName());
			statement.setString(3, member.getMobile());
			statement.setString(4, member.getEmail());
			return statement.executeUpdate() > 0;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public static boolean isBookExists(String id) {
		try {
			String checkstmt = "SELECT COUNT(*) FROM BOOK WHERE id=?";
			PreparedStatement stmt = DatabaseHandler.getInstance().getConnection().prepareStatement(checkstmt);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				LOGGER.info(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public static boolean isMemberExists(String id) {
		try {
			String checkstmt = "SELECT COUNT(*) FROM MEMBER WHERE id=?";
			PreparedStatement stmt = DatabaseHandler.getInstance().getConnection().prepareStatement(checkstmt);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				System.out.println(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public static ResultSet getBookInfoWithIssueData(String id) {
		try {
			String query = "SELECT BOOK.title, BOOK.author, BOOK.isAvail, ISSUE.issueTime FROM BOOK LEFT JOIN ISSUE on BOOK.id = ISSUE.bookID where BOOK.id = ?";
			PreparedStatement stmt = DatabaseHandler.getInstance().getConnection().prepareStatement(query);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			return rs;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return null;
	}

	public static void wipeTable(String tableName) {
		try {
			Statement statement = DatabaseHandler.getInstance().getConnection().createStatement();
			statement.execute("DELETE FROM " + tableName + " WHERE TRUE");
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
	}

	public static boolean updateMailServerInfo(MailServerInfo mailServerInfo) {
		try {
			wipeTable("MAIL_SERVER_INFO");
			PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
					"INSERT INTO MAIL_SERVER_INFO(server_name,server_port,user_email,user_password,ssl_enabled) VALUES(?,?,?,?,?)");
			statement.setString(1, mailServerInfo.getMailServer());
			statement.setInt(2, mailServerInfo.getPort());
			statement.setString(3, mailServerInfo.getEmailID());
			statement.setString(4, mailServerInfo.getPassword());
			statement.setBoolean(5, mailServerInfo.getSslEnabled());
			return statement.executeUpdate() > 0;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public static MailServerInfo loadMailServerInfo() {
		try {
			String checkstmt = "SELECT * FROM MAIL_SERVER_INFO";
			PreparedStatement stmt = DatabaseHandler.getInstance().getConnection().prepareStatement(checkstmt);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String mailServer = rs.getString("server_name");
				Integer port = rs.getInt("server_port");
				String emailID = rs.getString("user_email");
				String userPassword = rs.getString("user_password");
				Boolean sslEnabled = rs.getBoolean("ssl_enabled");
				return new MailServerInfo(mailServer, port, emailID, userPassword, sslEnabled);
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return null;
	}

	public static List<TaskListController.Task> loadTaskList() {
		return loadTaskList(null, null);
	}

    public static List<TaskLogListController.TaskLog> loadTaskLogList(String taskId) {
        return loadTaskLogList(taskId, null);
    }

	public static List<TaskListController.Task> loadTaskList(String taskId, String taskName) {
		StringBuilder sql = new StringBuilder("SELECT * FROM TASK WHERE 1=1");
		if (StringUtils.isNotBlank(taskId)) {
			sql.append(String.format(" AND ID = '%s'", taskId));
		}

		if (StringUtils.isNotBlank(taskName)) {
			sql.append(String.format(" AND NAME = '%s'", taskName));
		}
		return loadTask(sql.toString());
	}

	public static List<TaskLogListController.TaskLog> loadTaskLogList(String taskId, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM TASK_LOG WHERE 1=1");
        sql.append(String.format(" AND TASK_ID = '%s'", taskId));

        if (status != null) {
            sql.append(String.format(" AND STATUS = %d", status));
        }
        return loadLogTask(sql.toString());
	}

	private static List<TaskListController.Task> loadTask(String sql) {
		taskList.clear();
		DatabaseHandler handler = DatabaseHandler.getInstance();
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
		} catch (SQLException ex) {
			java.util.logging.Logger.getLogger(TaskListController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		return taskList;
	}

    private static List<TaskLogListController.TaskLog> loadLogTask(String sql) {
        taskLogList.clear();
        DatabaseHandler handler = DatabaseHandler.getInstance();
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
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(TaskListController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return taskLogList;
    }

	public static void removeTask(TaskListController.Task selectedForDeletion) {
		taskList.remove(selectedForDeletion);
	}
}
