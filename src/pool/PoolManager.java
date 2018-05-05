package pool;

import java.sql.Connection;

public class PoolManager{
	
	private static Pool myPool = null;
	
	public static void InitializePool(String URL) {
		myPool = new Pool(URL);
	}
	
	public static Connection getConnection() {
		return myPool.getConnection();				
	}
	
	public static void returnConnection(Connection c) {
		myPool.returnConnection(c);
	}
	
	// Getters
	public static int onlineConnections() {
		return myPool.getOnlineConnections();
	}
	public static int offlineConnections() {
		return myPool.getOfflineConnections();
	}
	public static int getInactiveUsers() {
		return myPool.getInactiveConnections();
	}
	public static int getActiveUsers() {
		return myPool.getActiveConnections();
	}
}
