package org.sensationcraft.login;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.sensationcraft.login.event.SCLoginRegisterEvent;
import org.sensationcraft.login.messages.Messages;
import org.sensationcraft.login.sql.Database;

import com.earth2me.essentials.Essentials;

public class PlayerManager
{

	public enum Status
	{

		NOT_REGISTERED, NOT_LOGGED_IN, AUTHENTICATED;
	}
	public Map<String, Status> playerStatus = new ConcurrentHashMap<String, Status>();
	public Set<String> loggingIn = Collections.synchronizedSet(new HashSet<String>());
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

	private Essentials ess;

	protected PlayerManager(final SCLogin plugin)
	{
		this.plugin = plugin;
		this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.updateIp = this.plugin.getConnection().prepare("UPDATE `players` SET `lastip` = ? WHERE LOWER(`username`) = LOWER(?)");
		this.register = this.plugin.getConnection().prepare("INSERT INTO `players`(`username`, `password`, `lastip`, `email`, `locked`) VALUES(LOWER(?), ?, ?, ?, ?)");
		this.unregister = this.plugin.getConnection().prepare("DELETE FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.isLocked = this.plugin.getConnection().prepare("SELECT locked FROM `players` WHERE LOWER(`username`) = LOWER(?)");
		this.setLock = this.plugin.getConnection().prepare("UPDATE `players` SET `locked` = ? WHERE LOWER(`username`) = LOWER(?)");
		this.getActiveCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` > 0");
		this.getLockedCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` = 0");

		this.safegaurdFile = new File(plugin.getDataFolder(), "safegaurd.dat");
		this.safegaurdCfg = YamlConfiguration.loadConfiguration(this.safegaurdFile);
		this.safeguarded.addAll(this.safegaurdCfg.getStringList("locked-to-ip"));

		final Plugin p = Bukkit.getPluginManager().getPlugin("Essentials");
		if(p != null)
			this.ess = (Essentials) p;
	}

	public boolean isRegistered(final String name)
	{
		final xAuthHook hook = this.plugin.getxAuthHook();
		final boolean isRegistered = Database.synchronizedExecuteQuery(Collections.<String, Object>emptyMap(), this.registered, this.registeredLock, name.toLowerCase());

		return isRegistered || (hook.isHooked() && hook.isRegistered(name));
	}

	public boolean hasRegistered(String name)
	{
		name = name.toLowerCase();
		Status status;
		status = this.playerStatus.get(name);
		return status == Status.NOT_LOGGED_IN;
	}

	public String getLastIp(final String name)
	{
		final Map<String, Object> results = new HashMap<String, Object>();
		results.put("lastip", null);
		if(!Database.synchronizedExecuteQuery(results, this.ip, this.ipLock, name))
			return "";
		return (String) results.get("lastip");
	}

	public boolean isLoggedIn(final String name)
	{
		return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
	}

	public boolean isOnline(final String name)
	{
		return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
	}

	public void join(final String name, final boolean isRegistered)
	{
		this.playerStatus.put(name.toLowerCase(), isRegistered ? ( Status.NOT_LOGGED_IN) : Status.NOT_REGISTERED);
	}

	public void quit(final String name)
	{
		this.playerStatus.remove(name.toLowerCase());
	}

	public void doLogin(final String name)
	{
		final Player player = this.plugin.getServer().getPlayerExact(name);
		if (player == null)
		{
			this.quit(name.toLowerCase());
			return;
		}
		this.playerStatus.put(name.toLowerCase(), Status.AUTHENTICATED);
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.sendMessage(Messages.LOGIN_SUCCESS.getMessage());
		Database.synchronizedExecuteUpdate(this.updateIp, this.updateIpLock, player.getAddress().getAddress().getHostAddress(), name.toLowerCase());
	}

	public boolean register(final String name, final String pass, final String ip) throws Exception
	{
		if (this.isRegistered(name))
			return false;

		Database.synchronizedExecuteUpdate(this.register, this.registerLock, name, pass, ip, "", 0);

		final boolean ret = this.isRegistered(name);
		if(ret)
			Bukkit.getPluginManager().callEvent(new SCLoginRegisterEvent(name, false));
		return ret;
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
		final Map<String, Object> results = new HashMap<String, Object>();
		results.put("locked", null);
		if(!Database.synchronizedExecuteQuery(results, this.isLocked, this.isLockedLock, name.toLowerCase()))
			return false;
		return (Integer)results.get("locked") > 0;
	}

	public boolean setLocked(final String name, final boolean flag)
	{
		Database.synchronizedExecuteUpdate(this.setLock, this.setLockLock, (flag ? 1 : 0), name.toLowerCase());
		return this.isLocked(name);
	}

	public boolean setLockedToIp(String name, final boolean flag)
	{
		name = name.toLowerCase();
		synchronized (this.safegaurdLock)
		{
			if (flag == this.safeguarded.contains(name))
				return false;
			if (flag)
				this.safeguarded.add(name);
			else
				this.safeguarded.remove(name);
		}
		this.safegaurdCfg.set("locked-to-ip", new ArrayList<String>(this.safeguarded));

		try
		{
			this.safegaurdCfg.save(this.safegaurdFile);
		}
		catch (final Exception ex)
		{
			// Swallow the exception
		}
		return true;
	}

	public boolean isLockedToIp(final String name)
	{
		synchronized (this.safegaurdLock)
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
		final Map<String, Object> results = new HashMap<String, Object>();
		results.put("count", null);

		if (what.startsWith("a"))
		{
			if(!Database.synchronizedExecuteQuery(results, this.getActiveCount, this.getActiveCountLock))
				return null;
		}
		else if (what.startsWith("l"))
			if(!Database.synchronizedExecuteQuery(results, this.getLockedCount, this.getLockedCountLock))
				return null;

		String r = null;
		try
		{
			r =  String.format("%d", (Integer)results.get("count"));
		}
		catch (final IllegalFormatException ex)
		{
			// Swallow the exception
		}
		return r;
	}

	public boolean isVisible(final Player object)
	{
		if(this.ess == null) return false;
		if(object == null) return false;
		return !this.ess.getVanishedPlayers().contains(object.getName());
	}

	public boolean isLoggingIn(final String name){
		return this.loggingIn.contains(name);
	}
}
