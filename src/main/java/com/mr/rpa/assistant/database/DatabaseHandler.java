package com.mr.rpa.assistant.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import com.mr.rpa.assistant.data.model.SysConfig;
import com.mr.rpa.assistant.database.impl.TaskDaoImpl;
import com.mr.rpa.assistant.database.impl.TaskLogDaoImpl;
import com.mr.rpa.assistant.ui.listbook.BookListController;
import com.mr.rpa.assistant.ui.listmember.MemberListController;
import com.mr.rpa.assistant.ui.settings.GlobalProperty;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DatabaseHandler {

	private final static Logger LOGGER = Logger.getLogger(DatabaseHandler.class);

	private static DatabaseHandler handler = null;

	private static final String DB_URL = String.format("jdbc:derby:%s;create=true",
			GlobalProperty.getInstance().getSysConfig().getDbPath());
	private static Connection conn = null;
	private static Statement stmt = null;

	private static TaskDao taskDao = new TaskDaoImpl();
	private static TaskLogDao taskLogDao= new TaskLogDaoImpl();

	static {
		createConnection();
		inflateDB();
		DatabaseHandler.getInstance().initSysConfig();
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
			LOGGER.info("准备装载数据库表: " + loadedTables);
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
					String createSQL = String.format("CREATE TABLE %s (%s)", tableName, query);
					tableData.add(createSQL);
					LOGGER.info(String.format("创建表成功，SQL:[%s]", createSQL));
				}
			}
			if (tableData.isEmpty()) {
				LOGGER.info("数据库表已经载入");
			} else {
				LOGGER.info("开始创建表结构.");
				createTables(tableData);
			}
		} catch (Exception ex) {
			LOGGER.error("", ex);
		}
	}

	private static void createConnection() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			conn = DriverManager.getConnection(DB_URL);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "无法载入数据库", "数据库错误", JOptionPane.ERROR_MESSAGE);
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
			LOGGER.error("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
			return null;
		} finally {
		}
		return result;
	}

	public void closeStmt() throws SQLException {
		stmt.close();
	}

	public boolean execAction(String qu) {
		try {
			stmt = conn.createStatement();
			stmt.execute(qu);
			return true;
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Error:" + ex.getMessage(), "Error Occured", JOptionPane.ERROR_MESSAGE);
			LOGGER.error("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
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
			LOGGER.error("{}", ex);
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
				LOGGER.info(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
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
			LOGGER.error("{}", ex);
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
				LOGGER.error(count);
				return (count > 0);
			}
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
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
			LOGGER.error("{}", ex);
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
			LOGGER.error("{}", ex);
		}
		return false;
	}

	private static void createTables(List<String> tableData) throws SQLException {
		Statement statement = conn.createStatement();
		statement.closeOnCompletion();
		for (String command : tableData) {
			LOGGER.info(command);
			statement.addBatch(command);
		}
		statement.executeBatch();
	}

	public Connection getConnection() {
		return conn;
	}

	public boolean initSysConfig() {
		try {
			SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
			ResultSet rs = execQuery("SELECT * FROM SYS_CONFIG");
			if (rs.next()) {
				sysConfig.setAdminUsername(rs.getString("admin_username"));
				sysConfig.setAdminPassword(rs.getString("admin_password"));
				sysConfig.setMailServerName(rs.getString("mail_server_name"));
				sysConfig.setMailSmtpPort(rs.getInt("mail_server_port"));
				sysConfig.setMailEmailAddress(rs.getString("mail_user_email"));
				sysConfig.setMailEmailPassword(rs.getString("mail_user_password"));
				sysConfig.setMailSslCheckbox(rs.getBoolean("mail_ssl_enabled"));
				sysConfig.setTaskFilePath(rs.getString("task_file_path"));
				sysConfig.setLogPath(rs.getString("log_path"));
				sysConfig.setControlServer(rs.getString("control_server"));
				sysConfig.setDbPath(rs.getString("db_path"));
				sysConfig.setMiniteErrorLimit(rs.getInt("minite_error_limit"));
				sysConfig.setRunningLimit(rs.getInt("running_limit"));
				rs.close();
				stmt.close();
			} else {
				insertSysConfig();
			}
			return true;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	public boolean updateSysConfig() {
		try {
			String deleteStatement = "DELETE FROM SYS_CONFIG";
			PreparedStatement stmt = conn.prepareStatement(deleteStatement);
			stmt.executeUpdate();
			insertSysConfig();
			stmt.close();
			return true;
		} catch (SQLException ex) {
			LOGGER.error("{}", ex);
		}
		return false;
	}

	private void insertSysConfig() throws SQLException {
		SysConfig sysConfig = GlobalProperty.getInstance().getSysConfig();
		PreparedStatement statement = DatabaseHandler.getInstance().getConnection().prepareStatement(
				"INSERT INTO SYS_CONFIG(id," +
						"admin_username," +
						"admin_password," +
						"mail_server_name," +
						"mail_server_port," +
						"mail_user_email," +
						"mail_user_password," +
						"mail_ssl_enabled," +
						"task_file_path," +
						"log_path," +
						"db_path," +
						"control_server," +
						"minite_error_limit," +
						"running_limit) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		int i = 1;
		statement.setString(i++, UUID.randomUUID().toString());
		//用户
		statement.setString(i++, sysConfig.getAdminUsername());
		statement.setString(i++, sysConfig.getAdminPassword());
		//邮箱
		statement.setString(i++, sysConfig.getMailServerName());
		statement.setInt(i++, sysConfig.getMailSmtpPort());
		statement.setString(i++, sysConfig.getMailEmailAddress());
		statement.setString(i++, sysConfig.getMailEmailPassword());
		statement.setBoolean(i++, sysConfig.getMailSslCheckbox());
		//path
		statement.setString(i++, sysConfig.getTaskFilePath());
		statement.setString(i++, sysConfig.getLogPath());
		statement.setString(i++, sysConfig.getDbPath());
		//控制中心
		statement.setString(i++, sysConfig.getControlServer());
		//预警
		statement.setInt(i++, sysConfig.getMiniteErrorLimit());
		statement.setInt(i++, sysConfig.getRunningLimit());
		statement.executeUpdate();
		statement.close();
	}

	public TaskDao getTaskDao(){
		return taskDao;
	}

	public TaskLogDao getTaskLogDao(){
		return taskLogDao;
	}
}
