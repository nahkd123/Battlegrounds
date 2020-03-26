package me.nahkd.spigot.btg.net;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class MySQLDatabase {
	
	String host, database, username, password;
	int port;
	
	public Connection connection;
	public HashSet<String> availableWeapons;
	
	public MySQLDatabase(String host, int port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		
		connection = null;
	}

	/**
	 * Open connection to database
	 * @throws SQLException idk
	 */
	public void openConnection() throws SQLException {
		if (connection != null && !connection.isClosed()) return;
		
		synchronized (this) {
			if (connection != null && !connection.isClosed()) return;
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				System.err.println("MySQL Driver is not installed!");
				System.err.println("Detail information: ClassNotFoundException throws (com.mysql.jdbc.Driver)");
			}
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
			
			availableWeapons = new HashSet<String>();
			ResultSet weapons = connection.createStatement().executeQuery("DESCRIBE weaponskins;");
			while (weapons.next()) {
				String str = weapons.getString("Field");
				// if (str != "uuid") availableWeapons.add(str);
				// How this crap doesn't work?
				availableWeapons.add(str);
			}
			if (availableWeapons.contains("uuid")) availableWeapons.remove("uuid");
			System.out.println(availableWeapons.size() + " available weapons. (Database Table: weaponskins)");
		}
	}
	
	/**
	 * Get selected weapon skin name
	 * @param uuid
	 * @param weapon
	 * @return
	 * @deprecated This might affect the performance. See {@link MySQLDatabase#getSkinFor(UUID, HashMap)}
	 */
	@Deprecated
	public String getSkinFor(UUID uuid, String weapon) {
		try {
			if (connection == null || connection.isClosed()) return "Default";
			ResultSet result = connection.createStatement().executeQuery("SELECT " + weapon + " FROM weaponskins WHERE uuid = '" + uuid.toString() + "';");
			if (result.next()) return result.getString(weapon);
			else return "Default";
		} catch (SQLException e) {
			e.printStackTrace();
			return "Default";
		}
	}
	
	public void getSkinFor(UUID uuid, HashMap<String, String> skinMap) {
		try {
			if (connection == null || connection.isClosed()) return;
			ResultSet result = connection.createStatement().executeQuery("SELECT * FROM weaponskins WHERE uuid = '" + uuid.toString() + "';");
			if (result.next()) {
				for (String weaponName : availableWeapons) skinMap.put(weaponName, result.getString(weaponName));
			} else return;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void addRankingPoints(UUID uuid, int points) {
		try {
			if (connection == null || connection.isClosed()) return;
			ResultSet result = connection.createStatement().executeQuery("SELECT rankingPoints FROM info WHERE uuid = '" + uuid.toString() + "';");
			if (result.next()) connection.createStatement().executeUpdate("UPDATE info SET rankingPoints = (rankingPoints + " + points + ") WHERE uuid = '" + uuid.toString() + "';");
			else connection.createStatement().executeUpdate("INSERT INTO info (uuid, rankingPoints) VALUES ('" + uuid.toString() + "', " + points + ");");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public int getRankingPoints(UUID uuid) {
		try {
			if (connection == null || connection.isClosed()) return 0;
			ResultSet result = connection.createStatement().executeQuery("SELECT rankingPoints FROM info WHERE uuid = '" + uuid.toString() + "'");
			if (result.next()) return result.getInt("rankingPoints");
			else return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
}
