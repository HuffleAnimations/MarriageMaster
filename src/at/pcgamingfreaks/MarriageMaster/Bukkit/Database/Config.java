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

import at.pcgamingfreaks.yaml.YAML;
import at.pcgamingfreaks.yaml.YAMLNotInitializedException;

import java.io.File;
import java.io.FileNotFoundException;

public class Config
{
	private YAML config = null;

	public Config()
	{
		try
		{
			config = new YAML(new File("config.yml"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			config = null;
		}
	}

	public boolean isLoaded()
	{
		return config != null;
	}

	public String getMySQLHost()
	{
		return config.getString("Database.MySQL.Host", "localhost");
	}

	public String getMySQLDatabase()
	{
		return config.getString("Database.MySQL.Database", "minecraft");
	}

	public String getMySQLUser()
	{
		return config.getString("Database.MySQL.User", "minecraft");
	}

	public String getMySQLPassword()
	{
		return config.getString("Database.MySQL.Password", "minecraft");
	}

	public String getUserTable()
	{
		return config.getString("Database.Tables.User", "marry_players");
	}

	public String getHomesTable()
	{
		return config.getString("Database.Tables.Home", "marry_home");
	}

	public String getPriestsTable()
	{
		return config.getString("Database.Tables.Priests", "marry_priests");
	}

	public String getPartnersTable()
	{
		return config.getString("Database.Tables.Partner", "marry_partners");
	}

	public boolean getUseUUIDs()
	{
		return config.getBoolean("UseUUIDs", true);
	}

	public boolean getUseMinepacks()
	{
		return config.getBoolean("UseMinepacks", false);
	}

	public boolean getSurname()
	{
		return config.getBoolean("Surname", false);
	}

	public void setDatabaseType()
	{
		config.set("Database.Type", "mysql");
		try
		{
			config.save(new File("config.yml"));
		}
		catch(YAMLNotInitializedException | FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getMySQLProperties()
	{
		List<String> list = config.getStringList("Database.MySQL.Properties", new LinkedList<String>());
		StringBuilder str = new StringBuilder();
		if(list != null)
		{
			for(String s : list)
			{
				str.append("&").append(s);
			}
		}
		return str.toString();
	}
}
