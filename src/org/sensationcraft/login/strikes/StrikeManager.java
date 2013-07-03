package org.sensationcraft.login.strikes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;
import org.sensationcraft.login.sql.Database;

public class StrikeManager
{

	private final SCLogin plugin;
	private final PreparedStatement lockip;
	private final Object lockipLock = new Object();
	private final PreparedStatement checkip;
	private final Object checkipLock = new Object();
	private final PreparedStatement dellock;
	private final Object dellockLock = new Object();
	Map<String, Integer> lowPriority = new HashMap<String, Integer>();
	Map<String, Integer> highPriority = new HashMap<String, Integer>();
	private final int MAX_POINTS = 100;
	private final int TEMP_LOCKOUT = 1000 * 60 * 10;
	private final String timeout;

	public StrikeManager(final SCLogin plugin)
	{
		this.plugin = plugin;
		this.lockip = this.plugin.getConnection().prepare("INSERT INTO `lockouts`(`ip`, `till`) VALUES(?,?)");
		this.checkip = this.plugin.getConnection().prepare("SELECT `till` FROM `lockouts` WHERE `ip` = ?");
		this.dellock = this.plugin.getConnection().prepare("DELETE FROM `lockouts` WHERE `ip` = ?");
		final StringBuilder to = new StringBuilder("Too many illegal activities while not logged in. Your ip has been temporarily locked out for");
		final int y = (int) Math.floor(this.TEMP_LOCKOUT / (1000 * 3600 * 24 * 365));
		final int w = (int) Math.floor((this.TEMP_LOCKOUT - (1000 * 3600 * 24 * 365 * y)) / (1000 * 3600 * 24 * 7));
		final int d = (int) Math.floor((this.TEMP_LOCKOUT - (1000 * 3600 * 24 * 365 * y) - (1000 * 3600 * 24 * 7 * w)) / (1000 * 3600 * 24));
		final int h = (int) Math.floor((this.TEMP_LOCKOUT - (1000 * 3600 * 24 * 365 * y) - (1000 * 3600 * 24 * 7 * w) - (1000 * 3600 * 24 * d)) / (1000 * 3600));
		final int m = (int) Math.floor((this.TEMP_LOCKOUT - (1000 * 3600 * 24 * 365 * y) - (1000 * 3600 * 24 * 7 * w) - (1000 * 3600 * 24 * d) - (1000 * 3600 * h)) / (1000 * 60));
		final int s = (int) Math.floor((this.TEMP_LOCKOUT - (1000 * 3600 * 24 * 365 * y) - (1000 * 3600 * 24 * 7 * w) - (1000 * 3600 * 24 * d) - (1000 * 3600 * h) - (1000 * 60 * m)) / (1000));

		if (y > 0)
			to.append(" ").append(y).append(" years");
		if (w > 0)
			to.append(" ").append(w).append(" weeks");
		if (d > 0)
			to.append(" ").append(d).append(" days");
		if (h > 0)
			to.append(" ").append(h).append(" hours");
		if (m > 0)
			to.append(" ").append(m).append(" minutes");
		if (s > 0)
			to.append(" ").append(s).append(" seconds");

		this.timeout = to.toString();

		System.out.println("Timeout set to: " + this.timeout);
	}

	public void addStrikePoints(final Player player, final int points, final boolean highPriority)
	{
		final Map<String, Integer> strikePoints = highPriority ? this.highPriority : this.lowPriority;

		final String name = player.getName().toLowerCase();
		if (!strikePoints.containsKey(name))
			strikePoints.put(name, points);
		else
			strikePoints.put(name, points + strikePoints.get(name));

		final String ip = player.getAddress().getAddress().getHostAddress();
		if (strikePoints.get(name) > this.MAX_POINTS)
		{
			this.resetStrikePoints(name, highPriority);
			if (highPriority)
			{
				System.out.println(System.currentTimeMillis());
				System.out.println(new Timestamp(System.currentTimeMillis()+this.TEMP_LOCKOUT).getTime());
				Database.synchronizedExecuteUpdate(this.lockip, this.lockipLock, ip, new java.sql.Timestamp(System.currentTimeMillis() + this.TEMP_LOCKOUT));
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						player.kickPlayer(String.format("Your ip has been banned for %s", StrikeManager.this.timeout));
					}
				}.runTask(this.plugin);

			}
			else
			{
				// Removed on request of Svesken
				/*new BukkitRunnable()
                 {
                 @Override
                 public void run()
                 {
                 player.kickPlayer("You are required to log in first!");
                 }
                 }.runTask(plugin);*/
			}
		} else
			player.sendMessage(Messages.NOT_LOGGEDIN.getMessage());
	}

	public void resetStrikePoints(final String name, final boolean highPriority)
	{
		if (highPriority)
			this.highPriority.remove(name);
		this.lowPriority.remove(name);
	}

	public boolean isIpLockedout(final String ip)
	{
		final ResultSet result = Database.synchronizedExecuteQuery(this.checkip, this.checkipLock, ip);
		try
		{
			if (result == null || !result.next())
			{
				System.out.println("Nothing found.");
				return false;
			}

			final Timestamp till = result.getTimestamp("till");
			final long now = System.currentTimeMillis();
			final boolean done = till.before(new Timestamp(now));
			if (done)
				new BukkitRunnable()
			{
				@Override
				public void run()
				{
					Database.synchronizedExecuteUpdate(StrikeManager.this.dellock, StrikeManager.this.dellockLock, ip);
				}
			}.runTaskAsynchronously(this.plugin);
			return !done;
		}
		catch (final SQLException ex)
		{
			ex.printStackTrace();
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
}
