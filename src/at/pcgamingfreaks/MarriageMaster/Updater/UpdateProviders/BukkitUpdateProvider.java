/*
 *   Copyright (C) 2016 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders;

import at.pcgamingfreaks.MarriageMaster.Updater.UpdateResult;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitUpdateProvider implements UpdateProvider
{
	//region static stuff
	private static final String USER_AGENT = "Plugin-Updater";
	private static final String HOST = "https://api.curseforge.com/servermods/files?projectIds=";
	private static final String VERSION_DELIMITER = "^[vV]|[\\s_-][vV]"; // Used for locating version numbers in file names, bukkit doesn't provide the version on it's own :(
	//endregion

	private final int projectID;
	private final String apiKey;
	private URL url = null;

	private Version[] versions = null;

	public BukkitUpdateProvider(int projectID)
	{
		this(projectID, null);
	}

	public BukkitUpdateProvider(int projectID, String apiKey)
	{
		this.projectID = projectID;
		this.apiKey = apiKey;

		try
		{
			url = new URL(HOST + projectID);
		}
		catch(MalformedURLException ignored) {}
	}

	@Override
	public UpdateResult query(Logger logger)
	{
		if(url == null) return UpdateResult.FAIL_FILE_NOT_FOUND;
		try
		{
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			if(apiKey != null) connection.addRequestProperty("X-API-Key", apiKey);
			connection.addRequestProperty("User-Agent", USER_AGENT);
			connection.setDoOutput(true);

			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				versions = new Gson().fromJson(reader, Version[].class);
				if(versions == null || versions.length == 0)
				{
					logger.warning("The updater could not find any files for the project id " + projectID);
					return UpdateResult.FAIL_FILE_NOT_FOUND;
				}
			}
		}
		catch(final IOException e)
		{
			if(e.getMessage().contains("HTTP response code: 403"))
			{
				logger.severe("dev.bukkit.org rejected the provided API key!");
				logger.severe("Please double-check your configuration to ensure it is correct.");
				logger.log(Level.SEVERE, null, e);
				return UpdateResult.FAIL_API_KEY;
			}
			else
			{
				logger.severe("The updater could not contact dev.bukkit.org for updating!");
				logger.severe("If this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
				logger.log(Level.SEVERE, null, e);
				return UpdateResult.FAIL_SERVER_OFFLINE;
			}
		}
		return UpdateResult.SUCCESS;
	}

	private class Version
	{
		@SuppressWarnings("unused")
		public String name, downloadUrl, fileName, fileUrl, releaseType, gameVersion, md5, projectId;
	}

	//region provider property's
	@Override
	public boolean provideDownloadURL()
	{
		return true;
	}

	@Override
	public boolean provideMD5Checksum()
	{
		return true;
	}
	//endregion

	//region getter for the latest version
	@Override
	public String getLatestVersion() throws NotSuccessfullyQueriedException
	{
		String name = getLatestName();
		String[] help = name.split(VERSION_DELIMITER);
		if(help.length == 2)
		{
			return help[1].split("\\s+")[0];
		}
		return null;
	}

	@Override
	public String getLatestVersionFileName() throws NotSuccessfullyQueriedException
	{
		if(versions == null)
		{
			throw new NotSuccessfullyQueriedException();
		}
		return versions[versions.length - 1].fileName;
	}

	@Override
	public URL getLatestFileURL() throws NotSuccessfullyQueriedException
	{
		if(versions == null)
		{
			throw new NotSuccessfullyQueriedException();
		}
		try
		{
			return new URL(versions[versions.length - 1].downloadUrl);
		}
		catch(MalformedURLException e)
		{
			System.out.println("Failed to interpret download url \"" + versions[versions.length - 1].downloadUrl + "\"!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getLatestName() throws NotSuccessfullyQueriedException
	{
		if(versions == null)
		{
			throw new NotSuccessfullyQueriedException();
		}
		return versions[versions.length - 1].name;
	}

	@Override
	public String getLatestChecksum() throws NotSuccessfullyQueriedException
	{
		if(versions == null)
		{
			throw new NotSuccessfullyQueriedException();
		}
		return versions[versions.length - 1].md5;
	}
	//endregion
}