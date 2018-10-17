/*
 * Copyright (C) 2014-2015 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import java.sql.*;

public class MySQL
{
	private Connection conn = null;
	private Config config;

	public MySQL(Config config)
	{
		this.config = config;
		checkDB();
	}

	private Connection getConnection()
	{
		try
		{
			if(conn == null || conn.isClosed())
			{
				System.out.print("Connecting with MySQL database.\n");
				conn = DriverManager.getConnection("jdbc:mysql://" + config.getMySQLHost() + "/" + config.getMySQLDatabase() + "?allowMultiQueries=true" + config.getMySQLProperties(), config.getMySQLUser(), config.getMySQLPassword());
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
	}

	private void checkDB()
	{
		try(Statement stmt = getConnection().createStatement())
		{
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + config.getUserTable() + "` (`player_id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(20) NOT NULL UNIQUE, PRIMARY KEY (`player_id`));");
			if(config.getUseUUIDs())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + config.getUserTable() + "` ADD COLUMN `uuid` CHAR(32) UNIQUE;");
				}
				catch(SQLException e)
				{
					if(e.getErrorCode() == 1142)
					{
						System.out.print(e.getMessage());
					}
					else if(e.getErrorCode() != 1060)
					{
						e.printStackTrace();
					}
				}
			}
			if(config.getUseMinepacks())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + config.getUserTable() + "` ADD COLUMN `sharebackpack` TINYINT(1) NOT NULL DEFAULT false;");
				}
				catch(SQLException e)
				{
					if(e.getErrorCode() == 1142)
					{
						System.out.print(e.getMessage());
					}
					else if(e.getErrorCode() != 1060)
					{
						e.printStackTrace();
					}
				}
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + config.getPriestsTable() + "` (`priest_id` INT NOT NULL, PRIMARY KEY (`priest_id`));");
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + config.getPartnersTable() + "` (`marry_id` INT NOT NULL AUTO_INCREMENT, `player1` INT NOT NULL, `player2` INT NOT NULL, `priest` INT NULL, `pvp_state` TINYINT(1) NOT NULL DEFAULT false, `date` DATETIME NOT NULL, PRIMARY KEY (`marry_id`) );");
			if(config.getSurname())
			{
				try
				{
					stmt.execute("ALTER TABLE `" + config.getPartnersTable() + "` ADD COLUMN `Surname` VARCHAR(35) UNIQUE;");
				}
				catch(SQLException e)
				{
					if(e.getErrorCode() == 1142)
					{
						System.out.print(e.getMessage());
					}
					else if(e.getErrorCode() != 1060)
					{
						e.printStackTrace();
					}
				}
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + config.getHomesTable() + "` (`marry_id` INT NOT NULL, `home_x` DOUBLE NOT NULL, `home_y` DOUBLE NOT NULL, `home_z` DOUBLE NOT NULL, `home_world` VARCHAR(45) NOT NULL DEFAULT 'world', PRIMARY KEY (`marry_id`) );");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void addPlayer(Player player)
	{
		try
		{
			PreparedStatement addPlayer;
			if(config.getUseUUIDs() && config.getUseMinepacks())
			{
				addPlayer = getConnection().prepareStatement("INSERT INTO `" + config.getUserTable() + "` (`name`,`uuid`,`sharebackpack`) VALUES (?,?,?);", Statement.RETURN_GENERATED_KEYS);
				addPlayer.setString(2, player.uuid);
				addPlayer.setBoolean(3, player.shareBackpack);
			}
			else if(config.getUseUUIDs())
			{
				addPlayer = getConnection().prepareStatement("INSERT INTO `" + config.getUserTable() + "` (`name`,`uuid`) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
				addPlayer.setString(2, player.uuid);
			}
			else if(config.getUseMinepacks())
			{
				addPlayer = getConnection().prepareStatement("INSERT INTO `" + config.getUserTable() + "` (`name`,`sharebackpack`) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
				addPlayer.setBoolean(2, player.shareBackpack);
			}
			else
			{
				addPlayer = getConnection().prepareStatement("INSERT INTO `" + config.getUserTable() + "` (`name`) VALUE (?);", Statement.RETURN_GENERATED_KEYS);
			}
			addPlayer.setString(1, player.name);
			addPlayer.executeUpdate();
			try(ResultSet rs = addPlayer.getGeneratedKeys())
			{
				if(rs.next())
				{
					player.id = rs.getInt(1);
				}
				else
				{
					System.out.print("No auto ID for player \"" + player.name + "\", try to load id from database ...\n");
					addPlayer = getConnection().prepareStatement("SELECT `player_id` FROM `" + config.getUserTable() + "` WHERE " + (config.getUseUUIDs() ? "`uuid`" : "`name`") + "=?;");
					addPlayer.setString(1, (config.getUseUUIDs() ? player.uuid : player.name));
					try(ResultSet rs2 = addPlayer.executeQuery())
					{
						if(rs2.next())
						{
							player.id = rs.getInt(2);
						}
						else
						{
							System.out.print("No ID for player \"" + player.name + "\", there is something wrong with this player! You should check that!!!\n");
						}
					}
				}
			}
			if(player.priest && player.id >= 0)
			{
				try(PreparedStatement addPriest = getConnection().prepareStatement("INSERT INTO `" + config.getPriestsTable() + "` (`priest_id`) VALUE (?);"))
				{
					addPriest.setInt(1, player.id);
					addPriest.execute();
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.out.print("Failed adding player \"" + player.name + "\"!\n");
		}
	}

	public void addMarriage(Marriage marriage)
	{
		if(marriage.player1 == null || marriage.player2 == null || marriage.player1.id < 0 || marriage.player2.id < 0) return;
		try
		{
			int i = 4;
			PreparedStatement addMarriage, addHome;
			if(config.getSurname())
			{
				addMarriage = getConnection().prepareStatement("INSERT INTO `" + config.getPartnersTable() + "` (`player1`,`player2`,`priest`,`Surname`,`pvp_state`,`date`) VALUES (?,?,?,?,?,NOW());", Statement.RETURN_GENERATED_KEYS);
				addMarriage.setString(i++, marriage.surname);
			}
			else
			{
				addMarriage = getConnection().prepareStatement("INSERT INTO `" + config.getPartnersTable() + "` (`player1`,`player2`,`priest`,`pvp_state`,`date`) VALUES (?,?,?,?,NOW());", Statement.RETURN_GENERATED_KEYS);
			}
			addMarriage.setInt(1, marriage.player1.id);
			addMarriage.setInt(2, marriage.player2.id);
			addMarriage.setObject(3, marriage.priest == null ? null : marriage.priest.id);
			addMarriage.setBoolean(i, marriage.pvpState);

			addMarriage.executeUpdate();
			try(ResultSet rs = addMarriage.getGeneratedKeys())
			{
				if(rs.next())
				{
					if(marriage.home != null)
					{
						int id = rs.getInt(1);
						addHome = getConnection().prepareStatement("INSERT INTO `" + config.getHomesTable() + "` (`marry_id`, `home_x`, `home_y`, `home_z`, `home_world`) VALUES (?,?,?,?,?);");
						addHome.setInt(1, id);
						addHome.setDouble(2, marriage.home.x);
						addHome.setDouble(3, marriage.home.y);
						addHome.setDouble(4, marriage.home.z);
						addHome.setString(5, marriage.home.world);
						addHome.execute();
					}
				}
				else
				{
					System.out.print("No ID for marriage \"" + marriage.player1.name + "<->" + marriage.player2.name + "\"!\n");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.print("Failed adding marriage \"" + marriage.player1.name + "<->" + marriage.player2.name + "\"!\n");
		}
	}
}
