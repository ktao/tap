/**
 * 
 */
package nl.wisdelft.twinder.io;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.wisdelft.twinder.utility.PropertyReader;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author ktao
 *
 */
public class JDBCDataSource {
	public DriverManagerDataSource ds = new DriverManagerDataSource();
	
	private Connection conn;

	public JDBCDataSource(String location, String username, String password) {
		ds.setDriverClassName(PropertyReader.getString("tal.db.mysql.driver"));
		ds.setUrl(location);
		ds.setUsername(username);
		ds.setPassword(password);
	}
	
	/**
	 * Executes the given query on the given data source
	 * 
	 * @param query
	 *            query to execute
	 * @param datasource
	 *            the data source where the query should be executed
	 * @return results of the query or <code>null</code> if an error occurred
	 */
	public final ResultSet executeQuerySingleConnection(String query,
			DriverManagerDataSource datasource) {
		Statement sqlStatement = null;
		try {
			if (conn == null || conn.isClosed()) {
				conn = datasource.getConnection();
			}
			sqlStatement = conn.createStatement();
			ResultSet res = sqlStatement.executeQuery(query);
			// datasource.getConnection().close();
			// sqlStatement.close();
			// conn.close();
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Simple way of using JDBCUtility
	 * @param query
	 * @return
	 */
	public final ResultSet executeQuerySingleConnection(String query) {
		return executeQuerySingleConnection(query, ds);
	}
	
	/**
	 * @param args
	 */
	public void main(String[] args) {
		
	}
}
