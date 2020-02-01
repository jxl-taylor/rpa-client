package com.mr.rpa.assistant.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mr.rpa.assistant.ui.listbook.BookListController;
import com.mr.rpa.assistant.ui.listtask.TaskListController;
import com.mr.rpa.assistant.ui.listmember.MemberListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DatabaseHandler {

	private final static Logger LOGGER = LogManager.getLogger(DatabaseHandler.class.getName());

	private static DatabaseHandler handler = null;

	private static final String DB_URL = "jdbc:derby:database;create=true";
	private static Connection conn = null;
	private static Statement stmt = null;

	static {
		createConnection();
		inflateDB();
	}

	private DatabaseHandler() {
	}

	public static DatabaseHandler getInstance() {
		if (handler == null) {
			handler = new DatabaseHandler();
		}
		return handler;
	}

	private static void inflateDB() {
		List<String> tableData = new ArrayList<>();
		try {
			Set<String> loadedTables = getDBTables();
			System.out.println("Already loaded tables " + loadedTables);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(DatabaseHandler.class.getClass().getResourceAsStream("/database/tables.xml"));
			NodeList nList = doc.getElementsByTagName("table-entry");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				Element entry = (Element) nNode;
				String tableName = entry.getAttribute("name");
				String query = entry.getAttribute("col-data");
				if (!loadedTables.contains(tableName.toLowerCase())) {
					tableData.add(String.format("CREATE TABLE %s (%s)", tableName, query));
				}
			}
			if (tableData.isEmpty()) {
				System.out.println("Tables are already loaded");
			} else {
				System.out.println("Inflating new tables.");
				createTables(tableData);
			}
		} catch (Exception ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
	}

	private static void createConnection() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			conn = DriverManager.getConnection(DB_URL);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Cant load database", "Database Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	private static Set<String> getDBTables() throws SQLException {
		Set<String> set = new HashSet<>();
		DatabaseMetaData dbmeta = conn.getMetaData();
		readDBTable(set, dbmeta, "TABLE", null);
		return set;
	}

	private static void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema) throws SQLException {
		ResultSet rs = dbmeta.getTables(null, schema, null, new String[]{searchCriteria});
		while (rs.next()) {
			set.add(rs.getString("TABLE_NAME").toLowerCase());
		}
	}

	public ResultSet execQuery(String query) {
		ResultSet result;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(query);
		} catch (SQLException ex) {
			System.out.println("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
			return null;
		} finally {
		}
		return result;
	}

	public boolean execAction(String qu) {
		try {
			stmt = conn.createStatement();
			stmt.execute(qu);
			return true;
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Error:" + ex.getMessage(), "Error Occured", JOptionPane.ERROR_MESSAGE);
			System.out.println("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
			return false;
		} finally {
		}
	}

	public boolean deleteBook(BookListController.Book book) {
		try {
			String deleteStatement = "DELETE FROM BOOK WHERE ID = ?";
			PreparedStatement stmt = conn.prepareStatement(deleteStatement);
			stmt.setString(1, book.getId());
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean deleteTask(TaskListController.Task task) {
		try {
			String deleteStatement = "DELETE FROM TASK WHERE ID = ?";
			PreparedStatement stmt = conn.prepareStatement(deleteStatement);
			stmt.setString(1, task.getId());
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean isBookAlreadyIssued(BookListController.Book book) {
		try {
			String checkstmt = "SELECT COUNT(*) FROM ISSUE WHERE bookid=?";
			PreparedStatement stmt = conn.prepareStatement(checkstmt);
			stmt.setString(1, book.getId());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				System.out.println(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean deleteMember(MemberListController.Member member) {
		try {
			String deleteStatement = "DELETE FROM MEMBER WHERE id = ?";
			PreparedStatement stmt = conn.prepareStatement(deleteStatement);
			stmt.setString(1, member.getId());
			int res = stmt.executeUpdate();
			if (res == 1) {
				return true;
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean isMemberHasAnyBooks(MemberListController.Member member) {
		try {
			String checkstmt = "SELECT COUNT(*) FROM ISSUE WHERE memberID=?";
			PreparedStatement stmt = conn.prepareStatement(checkstmt);
			stmt.setString(1, member.getId());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				System.out.println(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean updateBook(BookListController.Book book) {
		try {
			String update = "UPDATE BOOK SET TITLE=?, AUTHOR=?, PUBLISHER=? WHERE ID=?";
			PreparedStatement stmt = conn.prepareStatement(update);
			stmt.setString(1, book.getTitle());
			stmt.setString(2, book.getAuthor());
			stmt.setString(3, book.getPublisher());
			stmt.setString(4, book.getId());
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean updateTask(TaskListController.Task task) {
		try {
			String update = "UPDATE TASK SET NAME=?, DESP=? WHERE ID=?";
			PreparedStatement stmt = conn.prepareStatement(update);
			stmt.setString(1, task.getName());
			stmt.setString(2, task.getDesp());
			stmt.setString(3, task.getId());
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public boolean updateMember(MemberListController.Member member) {
		try {
			String update = "UPDATE MEMBER SET NAME=?, EMAIL=?, MOBILE=? WHERE ID=?";
			PreparedStatement stmt = conn.prepareStatement(update);
			stmt.setString(1, member.getName());
			stmt.setString(2, member.getEmail());
			stmt.setString(3, member.getMobile());
			stmt.setString(4, member.getId());
			int res = stmt.executeUpdate();
			return (res > 0);
		} catch (SQLException ex) {
			LOGGER.log(Level.ERROR, "{}", ex);
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		DatabaseHandler.getInstance();
	}

	public ObservableList<PieChart.Data> getTotalTaskGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String runSqL = "SELECT COUNT(*) FROM TASK WHERE RUNNING = TRUE ";
			String stopSql = "SELECT COUNT(*) FROM TASK WHERE RUNNING = FALSE ";
			ResultSet rs = execQuery(runSqL);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("已启动数 (" + count + ")", 100));
			}
			rs.close();
			stmt.close();
			rs = execQuery(stopSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("未启动数 (" + count + ")", 50));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public ObservableList<PieChart.Data> getTotalTaskLogGraphStatistics() {
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String succSqL = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 1 ";
			String failSql = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 2 ";
			ResultSet rs = execQuery(succSqL);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("成功总次数 (" + count + ")", 100));
			}
			rs.close();
			stmt.close();
			rs = execQuery(failSql);
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败总次数 (" + count + ")", 50));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public ObservableList<PieChart.Data> getTaskGraphStatistics(String taskId) {
		String sTaskId = StringUtils.isNotBlank(taskId) ? taskId : "undefined";
		ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
		try {
			String succSqL = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 1 AND TASK_id = '%s'";
			String failSql = "SELECT COUNT(*) FROM TASK_LOG WHERE STATUS = 2 AND TASK_id = '%s'";
			ResultSet rs = execQuery(String.format(succSqL, sTaskId));
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("成功次数 (" + count + ")", 100));
			}
			rs.close();
			stmt.close();
			rs = execQuery(String.format(failSql, sTaskId));
			if (rs.next()) {
				int count = rs.getInt(1);
				data.add(new PieChart.Data("失败次数 (" + count + ")", 50));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	private static void createTables(List<String> tableData) throws SQLException {
		Statement statement = conn.createStatement();
		statement.closeOnCompletion();
		for (String command : tableData) {
			System.out.println(command);
			statement.addBatch(command);
		}
		statement.executeBatch();
	}

	public Connection getConnection() {
		return conn;
	}
}
