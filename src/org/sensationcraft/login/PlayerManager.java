package org.sensationcraft.login;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.messages.Messages;
import org.sensationcraft.login.sql.Database;

public class PlayerManager
{

	public enum Status
	{
		NOT_REGISTERED, NOT_LOGGED_IN, AUTHENTICATED;
	}
	public Map<String, Status> playerStatus = new HashMap<String, Status>();
	private final Object statusLock = new Object();
	private final SCLogin plugin;
	private final PreparedStatement registered;
	private final Object registeredLock = new Object();
	private final PreparedStatement register;
	private final Object registerLock = new Object();
	private final PreparedStatement unregister;
	private final Object unregisterLock = new Object();
	private final PreparedStatement ip;
	private final Object ipLock = new Object();
	private final PreparedStatement updateIp;
	private final Object updateIpLock = new Object();
	private final PreparedStatement email;
	private final Object emailLock = new Object();
	private final PreparedStatement isLocked;
	private final Object isLockedLock = new Object();
	private final PreparedStatement setLock;
	private final Object setLockLock = new Object();
	private final PreparedStatement getActiveCount;
	private final Object getActiveCountLock = new Object();
	private final PreparedStatement getLockedCount;
	private final Object getLockedCountLock = new Object();

	private final File safegaurdFile;
	private final YamlConfiguration safegaurdCfg;
	private final Set<String> safeguarded = new HashSet<String>();
	private final Object safegaurdLock = new Object();

	protected PlayerManager(final SCLogin plugin)
	{
		this.plugin = plugin;
		this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.updateIp = this.plugin.getConnection().prepare("UPDATE `players` SET `lastip` = ? WHERE LOWER(`username`) = LOWER(?)");
		this.register = this.plugin.getConnection().prepare("INSERT INTO `players`(`username`, `password`, `lastip`, `email`, `locked`) VALUES(LOWER(?), ?, ?, ?, ?)");
		this.email = this.plugin.getConnection().prepare("SELECT `email` FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.unregister = this.plugin.getConnection().prepare("DELETE FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.isLocked = this.plugin.getConnection().prepare("SELECT locked FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.setLock = this.plugin.getConnection().prepare("UPDATE `players` SET `locked` = ? WHERE LOWER(`username`) = LOWER(?)");
		this.getActiveCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` > 0");
		this.getLockedCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` = 0");

		this.safegaurdFile = new File(plugin.getDataFolder(), "safegaurd.dat");
		this.safegaurdCfg = YamlConfiguration.loadConfiguration(this.safegaurdFile);
		this.safeguarded.addAll(this.safegaurdCfg.getStringList("locked-to-ip"));
	}

	public boolean isRegistered(final String name)
	{
		final xAuthHook hook = this.plugin.getxAuthHook();
		ResultSet result = null;
		try
		{
			this.registered.setString(1, name);
			result = Database.synchronizedExecuteQuery(this.registered, this.registeredLock, name);
			return (result != null && result.next()) || (hook.isHooked() && hook.isRegistered(name));
		}
		catch (final SQLException ex)
		{
			// Might log this
		}
		finally
		{
			if (result != null)
				try
			{
					result.close();
			}
			catch (final SQLException ex)
			{
			}
		}
		return false;
	}
        
        public boolean hasRegistered(String name)
        {
            name = name.toLowerCase();
            synchronized(this.statusLock)
            {
                return this.playerStatus.get(name) == Status.NOT_LOGGED_IN;
            }
        }

	public String getLastIp(final String name)
	{
		ResultSet result = null;
		try
		{
			result = Database.synchronizedExecuteQuery(this.ip, this.ipLock, name);
			if (!result.next())
				return "";
			return result.getString("lastip");
		}
		catch (final SQLException ex)
		{
		}
		finally
		{
			if (result != null)
				try
			{
					result.close();
			}
			catch (final SQLException ex)
			{
			}
		}
		return "";
	}

	public String getEmail(final String name)
	{
		ResultSet result = null;
		try
		{
			result = Database.synchronizedExecuteQuery(this.email, this.emailLock, name);
			if (!result.next())
				return "";
			return result.getString("email");
		}
		catch (final SQLException ex)
		{
			// Might log this
		}
		finally
		{
			if (result != null)
				try
			{
					result.close();
			}
			catch (final SQLException ex)
			{
			}
		}
		return "";
	}

	public boolean isLoggedIn(final String name)
	{
		synchronized (this.statusLock)
		{
			return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
		}
	}

	public boolean isOnline(final String name)
	{
		synchronized (this.statusLock)
		{
			return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
		}
	}

	public void join(final String name, boolean isRegistered)
	{
		synchronized (this.statusLock)
		{
			this.playerStatus.put(name.toLowerCase(), isRegistered ? Status.NOT_LOGGED_IN : Status.NOT_REGISTERED);
		}
	}

	public void quit(final String name)
	{
		synchronized (this.statusLock)
		{
			this.playerStatus.remove(name.toLowerCase());
		}
	}

	public void doLogin(final String name)
	{
		final Player player = this.plugin.getServer().getPlayerExact(name);
		if (player == null)
		{
			this.quit(name);
			return;
		}
		synchronized (this.statusLock)
		{
			this.playerStatus.put(name, Status.AUTHENTICATED);
		}
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.sendMessage(Messages.LOGIN_SUCCESS.getMessage());
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (final Player other : Bukkit.getOnlinePlayers())
					if (!other.canSee(player) && PlayerManager.this.isLoggedIn(other.getName().toLowerCase()))
						other.showPlayer(player);
			}
		}.runTask(this.plugin);
		Database.synchronizedExecuteUpdate(this.updateIp, this.updateIpLock, player.getAddress().getAddress().getHostAddress(), name);
	}

	public boolean register(final String name, final String pass, final String ip) throws Exception
	{
		if (this.isRegistered(name))
			return false;

		Database.synchronizedExecuteUpdate(this.register, this.registerLock, name, pass, ip, 0);

		return this.isRegistered(name);
	}

	public boolean unregister(final String name)
	{
		if (!this.isRegistered(name))
			return false;

		Database.synchronizedExecuteUpdate(this.unregister, this.unregisterLock, name);

		return !this.isRegistered(name);
	}

	public boolean isLocked(final String name)
	{
		final ResultSet result = Database.synchronizedExecuteQuery(this.isLocked, this.isLockedLock, name);
		try
		{
			if (result == null || !result.next())
				return false;
			return result.getInt("locked") > 0;
		}
		catch (final SQLException ex)
		{
			ex.printStackTrace();
			// Swallow the exception
		}
		return false;
	}

	public boolean setLocked(final String name, final boolean flag)
	{
		Database.synchronizedExecuteUpdate(this.setLock, this.setLockLock, (flag ? 1 : 0), name);
		return this.isLocked(name);
	}

	public boolean setLockedToIp(String name, final boolean flag)
	{
		name = name.toLowerCase();
		synchronized(this.safegaurdLock)
		{
			if(flag == this.safeguarded.contains(name))
				return false;
			if(flag) this.safeguarded.add(name);
			else this.safeguarded.remove(name);
		}
		this.safegaurdCfg.set("locked-to-ip", new ArrayList<String>(this.safeguarded));

		try
		{
			this.safegaurdCfg.save(this.safegaurdFile);
		}
		catch(final Exception ex)
		{
			// Swallow the exception
		}
		return true;
	}

	public boolean isLockedToIp(final String name)
	{
		synchronized(this.safegaurdLock)
		{
			return this.safeguarded.contains(name.toLowerCase());
		}
	}

	public String getProfile(final String name)
	{
		if (!this.isRegistered(name))
			return ChatColor.RED + "This player has not played before.";

		final StringBuilder profile = new StringBuilder("----- SCLogin profile -----\n");
		profile.append("Name: ").append(name).append("\n");
		final boolean isOnline = this.isOnline(name);
		profile.append("Online: ").append(this.getValueColour(isOnline)).append(isOnline).append(ChatColor.RESET).append("\n");
		final boolean isAuthenticated = this.isLoggedIn(name);
		profile.append("Authenticated: ").append(this.getValueColour(isAuthenticated)).append(isAuthenticated).append(ChatColor.RESET).append("\n");
		profile.append("IP: ").append(this.getLastIp(name)).append("\n");
		profile.append("Last authentication: ").append("not implemented yet").append("\n");
		final boolean isLocked = this.isLocked(name);
		profile.append("Locked: ").append(this.getValueColour(isLocked)).append(isLocked).append(ChatColor.RESET).append("\n");
		return profile.append("---------------------------").toString();
	}

	private ChatColor getValueColour(final boolean flag)
	{
		return flag ? ChatColor.GREEN : ChatColor.RED;
	}

	public String getCount(final String what)
	{
		if (what == null)
			return null;
		ResultSet result = null;
		if (what.startsWith("a"))
			result = Database.synchronizedExecuteQuery(this.getActiveCount, this.getActiveCountLock);
		else if (what.startsWith("l"))
			result = Database.synchronizedExecuteQuery(this.getLockedCount, this.getLockedCountLock);
		try
		{
			if (result != null && result.next())
				return String.format("%d", result.getInt("count"));
		}
		catch (final SQLException ex)
		{
			// Swallow the exception
		}
		catch (final IllegalFormatException ex)
		{
			// Swallow the exception
		}
		return null;
	}
}
