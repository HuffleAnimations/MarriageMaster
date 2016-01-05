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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Files
{
	private Config config;
	private Set<String> priests = new HashSet<>();
	private Set<Marriage> marriages = new HashSet<>();
	private Map<String, YAML> marryMap = new HashMap<>();
	private Map<String, Player> player = new HashMap<>();


	public Files(Config config)
	{
		this.config = config;
		player.put((config.getUseUUIDs()) ? "00000000000000000000000000000000" : "none", new Player("none", "00000000000000000000000000000000", false, false));
		player.put((config.getUseUUIDs()) ? "00000000000000000000000000000001" : "Console", new Player("Console", "00000000000000000000000000000001", false, false));
		loadPriests();
		loadAllPlayers();
		prepareData();
	}

	public void close()
	{
		System.out.print("Cleaning some memory ...");
		for(Map.Entry<String, YAML> stringYAMLEntry : marryMap.entrySet())
		{
			stringYAMLEntry.getValue().dispose();
		}
		priests.clear();
		marryMap.clear();
		player.clear();
		System.out.print("Finished cleaning memory.");
	}

	public Set<Marriage> getMarriages()
	{
		return marriages;
	}

	public Set<Player> getPlayers()
	{
		Set<Player> players = new HashSet<>();
		Iterator<Map.Entry<String, Player>> iterator = player.entrySet().iterator();
		Map.Entry<String, Player> e;
		while(iterator.hasNext())
		{
			e = iterator.next();
			players.add(e.getValue());
		}
		return players;
	}

	private void prepareData()
	{
		System.out.print("Preparing data for database ...");
		Iterator<Map.Entry<String, YAML>> iterator = marryMap.entrySet().iterator();
		Map.Entry<String, YAML> e;
		while(iterator.hasNext())
		{
			e = iterator.next();
			if(config.getUseUUIDs())
			{
				player.put(e.getKey(), new Player(e.getValue().getString("Name", ""), e.getKey(), e.getValue().getBoolean("isPriest", false), e.getValue().getBoolean("ShareBackpack", false)));
			}
			else
			{
				player.put(e.getKey(), new Player(e.getKey(), null, e.getValue().getBoolean("isPriest", false), e.getValue().getBoolean("ShareBackpack", false)));
			}
		}

		iterator = marryMap.entrySet().iterator();
		Set<String> checked = new HashSet<>();
		String s;
		while(iterator.hasNext())
		{
			e = iterator.next();
			if(checked.contains(e.getKey())) continue;
			if(config.getUseUUIDs())
			{
				s = e.getValue().getString("MarriedToUUID", "");
			}
			else
			{
				s = e.getValue().getString("MarriedTo", "");
			}
			Player p1 = player.get(e.getKey()), p2, p = (e.getValue().getString("MarriedBy", null) != null) ? player.get(e.getValue().getString("MarriedBy", null)) : null;
			p2 = player.get(s);
			checked.add(s);
			marriages.add(new Marriage(p1, p2, p, e.getValue().getString("MarriedDay", null), e.getValue().getString("Surname", null), e.getValue().getBoolean("PvP", false), getHome(e.getValue())));
		}
		System.out.print("Data prepared.");
	}

	private Home getHome(YAML yaml)
	{
		try
		{
			return new Home(yaml.getDouble("MarriedHome.location.X"), yaml.getDouble("MarriedHome.location.Y"), yaml.getDouble("MarriedHome.location.Z"), yaml.getString("MarriedHome.location.World"));
		}
		catch(Exception ignored) { }
		return null;
	}

	private void loadPriests()
	{
		System.out.print("Loading priests ...");
		File file = new File("priests.yml");
		priests.clear();
		if(file.exists())
		{
			try(FileReader fr = new FileReader(file); BufferedReader in = new BufferedReader(fr))
			{
				String str;
				while ((str = in.readLine()) != null)
				{
					if(!str.isEmpty())
					{
						priests.add(str.replace("\r", ""));
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.print("Priests loaded.");
	}

	private void loadAllPlayers()
	{
		System.out.print("Loading players ...");
		File file = new File("players");
		String temp;
		if(file.exists())
		{
			File[] allFiles = file.listFiles();
			if(allFiles != null && allFiles.length > 0)
			{
				for(File item : allFiles)
				{
					temp = item.getName();
					if(temp.endsWith(".yml"))
					{
						loadPlayer(temp.substring(0, temp.length() - 4));
					}
				}
			}
		}
		System.out.print("Players loaded.");
	}

	private void loadPlayer(String player)
	{
		File file = new File((new StringBuilder()).append("players").append(File.separator).append(player).append(".yml").toString());
		if(file.exists())
		{
			try
			{
				marryMap.put(player, new YAML(file));
				if(marryMap.get(player).getString("MarriedTo", "").equalsIgnoreCase(player))
				{
					marryMap.get(player).dispose();
					marryMap.remove(player);
					//noinspection ResultOfMethodCallIgnored
					file.delete();
				}
				else
				{
					marryMap.get(player).set("isPriest", priests.contains(player));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}