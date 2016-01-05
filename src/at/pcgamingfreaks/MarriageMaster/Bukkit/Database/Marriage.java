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

public class Marriage
{
	public Player player1, player2, priest;
	public String date, surname;
	public boolean pvpState;
	public Home home;

	public Marriage(Player p1, Player p2, Player p, String date, String surname, boolean pvp, Home home)
	{
		player1 = p1;
		player2 = p2;
		priest = p;
		this.date = date;
		this.surname = surname;
		pvpState = pvp;
		this.home = home;
	}
}