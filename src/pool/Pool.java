package pool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class Pool {
	
	private ArrayList<Connection> availableConnections = new ArrayList<Connection>();
	private Properties props = null;
	private int online = 0;
	private int offline = 0;
	
	public Pool(String url) {
		props = getProperties(url);
		offline = Integer.parseInt(props.getProperty("maxConnections"));
		initializeConnectionPool();
	}

	private void initializeConnectionPool() {
		while(!checkPoolIsAlmostFull()) {
			availableConnections.add(newConnection());
		}
	}

	private Connection newConnection() {
			try {
				Class.forName(props.getProperty("driver"));
				Connection connection = DriverManager.getConnection(props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
				online += 1;
				offline -= 1;
				return connection;
			} catch(SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		return null;
	}


	public synchronized Connection getConnection() {
		Connection connection = null;

		do{
			if(availableConnections.size() > 0) {
				connection = availableConnections.get(0);
				availableConnections.remove(0);
			}else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}while(connection == null);
			 
		checkMinConnections();
		return connection;
	}


	public void returnConnection(Connection connection) {
		availableConnections.add(connection);
		checkConnectionUnused();
	}


	private synchronized boolean checkPoolIsAlmostFull() {
		final int minSize = Integer.parseInt(props.getProperty("maxWaitingCnx"));
		if(availableConnections.size() < minSize) {
			return false;
		}
		return true;
	}


	private synchronized void checkMinConnections() {
		final int minSize = Integer.parseInt(props.getProperty("maxWaitingCnx"));
		if(availableConnections.size() < minSize) {
			while((availableConnections.size() < 5) && (offline > 0)){
				Connection connection = newConnection();
				availableConnections.add(connection);
			}
		}
	}


	private synchronized void checkConnectionUnused() {
		final int maxSize = Integer.parseInt(props.getProperty("maxWaitingCnx"));
		if(availableConnections.size() > maxSize) {
			while(availableConnections.size() > maxSize) {
				try {
					Connection connection = availableConnections.get(0);
					availableConnections.remove(0);
					connection.close();
					online -= 1;
					offline += 1;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Getters
	public int getOnlineConnections() {
		return online;
	}
	public int getOfflineConnections() {
		return offline;
	}
	public int getActiveConnections() {
		return (online - availableConnections.size());
	}
	public int getInactiveConnections() {
		return availableConnections.size();
	}

	private static Properties getProperties(String url) {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(url);
			prop.load(is);
		}catch(IOException e){
			System.out.println(e.toString());
		}
		return prop;
	}

}
