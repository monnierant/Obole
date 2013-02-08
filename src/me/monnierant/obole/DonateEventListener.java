package me.monnierant.obole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DonateEventListener implements Listener
{

	public Obole plugin;

	public DonateEventListener(Obole plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onDonate(DonateEvent event)
	{

		// Event Variables
		String user = event.getUsername();
		Double amount = event.getAmount();
		ResultSet r = plugin.getNewDonors();

		// Detect Duplicate Donations
		if (plugin.getDupes(user, plugin.parseAmount(amount).replace("$", "")) >= 2)
		{
			for (Player p : plugin.getServer().getOnlinePlayers())
			{
				if (p.hasPermission("donator.alert"))
				{
					p.sendMessage("§8-------------- §6Donator §8- §4Dupe Alert §8--------------");
					p.sendMessage("§cWarning! §7The player " + user + " has donated §a" + plugin.parseAmount(amount) + " §c(" + plugin.getDupes(user, plugin.parseAmount(amount).replace("$", "")) + ") times!");
					p.sendMessage("§7Please verify that the " + plugin.getDupes(user, plugin.parseAmount(amount).replace("$", "")) + " donations were intentional and refund the extra ones if needed.");
				}
			}
		}

		// Simple donation processing.
		plugin.getServer().broadcastMessage(plugin.colorize(plugin.getConfig().getString("settings.broadcast-message").replace("%player", event.getUsername()).replace("%amount", plugin.parseAmount(amount))));
		if (plugin.getConfig().getBoolean("settings.enablesignwall"))
		{
			plugin.updateSignWall(user, amount);
		}

		try
		{
			if (r != null)
			{
				while (r.next())
				{
					for (String pack : plugin.getConfig().getConfigurationSection("packages").getKeys(false))
					{
						String price = plugin.getConfig().getString("packages." + pack + ".price");
						List<String> commands = plugin.getConfig().getStringList("packages." + pack + ".commands");
						if (!plugin.getConfig().getBoolean("settings.cumulativepackages"))
						{
							if (amount.equals(price) || (amount + "0").equals(price))
							{
								r.updateString("expires", plugin.getExpiresDate(pack));
								for (String cmnd : commands)
								{
									plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", plugin.parseAmount(amount)));
								}
							}
						}
						else
						{
							Double total = plugin.getTotalDonated(r.getString("username")) + amount;
							if (total.equals(price) || (total + "0").equals(price))
							{
								r.updateString("expires", plugin.getExpiresDate(pack));
								for (String cmnd : commands)
								{
									plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", plugin.parseAmount(amount)));
								}
							}
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			if (plugin.debuggling)
				e.printStackTrace();
		}

	}
}
