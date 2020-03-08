package com.mr.rpa.assistant.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.mr.rpa.assistant.data.model.MailServerInfo;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author afsal
 */
@Log4j
@Component
public class DataHelper {

	@Resource
	private DatabaseHandler handler;

	public void wipeTable(String tableName) {
		try {
			Statement statement = handler.getConnection().createStatement();
			statement.execute("DELETE FROM " + tableName + " WHERE TRUE");
		} catch (SQLException ex) {
			log.error( ex);
		}
	}

	public boolean updateMailServerInfo(MailServerInfo mailServerInfo) {
		try {
			wipeTable("MAIL_SERVER_INFO");
			PreparedStatement statement = handler.getConnection().prepareStatement(
					"INSERT INTO MAIL_SERVER_INFO(server_name,server_port,user_email,user_password,ssl_enabled) VALUES(?,?,?,?,?)");
			statement.setString(1, mailServerInfo.getMailServer());
			statement.setInt(2, mailServerInfo.getPort());
			statement.setString(3, mailServerInfo.getEmailID());
			statement.setString(4, mailServerInfo.getPassword());
			statement.setBoolean(5, mailServerInfo.getSslEnabled());
			return statement.executeUpdate() > 0;
		} catch (SQLException ex) {
			log.error(ex);
		}
		return false;
	}

	public MailServerInfo loadMailServerInfo() {
		try {
			String checkstmt = "SELECT * FROM MAIL_SERVER_INFO";
			PreparedStatement stmt = handler.getConnection().prepareStatement(checkstmt);
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
			log.error(ex);
		}
		return null;
	}

}
