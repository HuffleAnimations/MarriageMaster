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

package at.pcgamingfreaks.MarriageMaster.Bukkit;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.*;

import java.util.Set;

public class Converter
{
	public static void main(String [] args)
	{
		System.out.print("Loading config file ...\n");
		Config config = new Config();
		if(!config.isLoaded()) { System.out.print("Failed to load config.yml! Make sure she is in the same folder!\n"); return; }
		System.out.print("Config file loaded.\n");

		Files f = new Files(config);
		Set<Player> players = f.getPlayers();
		Set<Marriage> marriages = f.getMarriages();
		f.close();

		MySQL mySQL = new MySQL(config);
		System.out.print("Writing players to MySQL database ...\n");
		for(Player p : players)
		{
			mySQL.addPlayer(p);
		}
		System.out.print("Finished writing players to MySQL database.\n");
		System.out.print("Writing marriages to MySQL database ...\n");
		for(Marriage m : marriages)
		{
			mySQL.addMarriage(m);
		}
		System.out.print("Finished writing marriages to MySQL database.\n");
		config.setDatabaseType();
		System.out.print("Finished migrating files to MySQL database.\n");
	}
}