/**
 * 
 */
package nl.wisdelft.twinder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import nl.wisdelft.twinder.utility.PropertyReader;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.object.BatchSqlUpdate;

/**
 * @author ktao
 *
 */
public class JDBCUtility {
	public static DriverManagerDataSource ds = new DriverManagerDataSource();
	
	private static Connection conn;

	static {
		ds.setDriverClassName(PropertyReader.getString("tal.db.mysql.driver")); //$NON-NLS-1$
		ds.setUrl(PropertyReader.getString("tal.db.mysql.location")); //$NON-NLS-1$
		ds.setUsername(PropertyReader.getString("tal.db.mysql.username")); //$NON-NLS-1$
		ds.setPassword(PropertyReader.getString("tal.db.mysql.password")); //$NON-NLS-1$
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
	public static final ResultSet executeQuerySingleConnection(String query,
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
	public static final ResultSet executeQuerySingleConnection(String query) {
		return executeQuerySingleConnection(query, ds);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public static void reserveEnglishTweets() {
		BatchSqlUpdate bsu = new BatchSqlUpdate(ds, "INSERT INTO tweets_en SELECT * FROM tweets WHERE id = ?");
		bsu.declareParameter(new SqlParameter("id", Types.BIGINT));
		bsu.compile();
		bsu.setBatchSize(1000);
		String filename = "/Users/ktao/Desktop/trec2011-id-content";
		int i = 0;
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(filename)));
			String line;
			while ((line = bf.readLine()) != null) {
				Long id = Long.parseLong(line.substring(0, line.indexOf(' ')));
				
				bsu.update(new Object[]{
						id
				});
				
				i++;
				if (i % 10000 == 0) {
					System.out.println(i + " lines transferred.");
					bsu.flush();
				}
			}
			bsu.flush();
			System.out.println(i + " lines transferred.");
			bf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
