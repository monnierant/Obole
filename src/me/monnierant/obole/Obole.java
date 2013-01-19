package me.monnierant.obole;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Obole extends JavaPlugin {

	// MySQL Connection Information
	Connection conn;
	String user, pass, url;
	Integer port;
	Boolean connectionfailed = true;
	Boolean debuggling = false;
	
	Translator m_translator=null;

	// Other Variables
	public final Logger log = Logger.getLogger("Minecraft");
	public static HashSet<String> settingwall = new HashSet<String>();

	public void onEnable() {
		// Generate Default Configuration
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			List<String> defaultcommands = new ArrayList<String>();
			List<String> defaultexpirescommands = new ArrayList<String>();
			List<String> signwall = new ArrayList<String>();
			defaultcommands.add("promote %player");
			defaultcommands.add("msg %player Thanks for donating %amount!");
			defaultexpirescommands.add("demote %player");
			signwall.add("This is line 1!");
			signwall.add("%player");
			signwall.add("%amount");
			getConfig().addDefault("packages.example.price", "5.00");
			getConfig().addDefault("packages.example.expires", "0");
			getConfig().addDefault("packages.example.expires", "0");
			getConfig().addDefault("signwall-format", signwall);
			getConfig().addDefault("packages.example.commands", defaultcommands);
			getConfig().addDefault("packages.example.expirescommands", defaultexpirescommands);
		}

		// Load Configuration
		getConfig().options().copyDefaults(true);
		saveConfig();
		debuggling = getConfig().getBoolean("settings.debug");
		
		
		// Connect to MySQL Database
		user = getConfig().getString("database.username");
		pass = getConfig().getString("database.password");
		url = "jdbc:mysql://" + getConfig().getString("database.hostname") + ":" + getConfig().getInt("database.port") + "/" + getConfig().getString("database.database_name");

		try {
			conn = DriverManager.getConnection(url, user, pass);
			connectionfailed = false;
		} catch (final Exception e) {
			getLogger().log(Level.SEVERE, "Could not connect to database! Verify your database details in the configuration are correct.");
			if (debuggling) e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}

		setupMysql();

		if (!connectionfailed) {
			// Register Events
			getServer().getPluginManager().registerEvents(new OboleListeners(this), this);
			getServer().getPluginManager().registerEvents(new DonateEventListener(this), this);

			// Start a task to check for new donations.
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					checkForDonations();
				}
			}, 200L, getConfig().getInt("settings.checkdelay") * 20);
			
			// Start a task to check for new donations.
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					checkForExpireDonations();
				}
			}, 720000L, getConfig().getInt("settings.checkExpiredDelay") * 20);
			
		}
	}

	public void onDisable() {
		// Save Configuration
		reloadConfig();
		saveConfig();

		// Cancel Tasks
		getServer().getScheduler().cancelTasks(this);
	}
	

	public void checkForExpireDonations() {
		if (!connectionfailed) {

			// Define Variables
			//ResultSet r = getNewDonors();
			ResultSet rx = getResultSet("SELECT * FROM donations WHERE expired='false'");

			// Check for and process new donations.
			/*
			try {
				while (r.next()) {
					String user = r.getString("username");
					Double amount = r.getDouble("amount");
					DonateEvent event = new DonateEvent(r.getString("username"), r.getDouble("amount"), r.getString("date"), r.getString("first_name"), r.getString("last_name"), r.getString("payer_email"), r.getString("expires"));
					getServer().getPluginManager().callEvent(event);
					r.updateString("processed", "true");
					r.updateRow();
					updateDonorPlayers(user, getTotalDonated(user) + amount);
				}
			} catch (Exception e) {
				log.severe("[Donator] Error encountered when checking for new donations!");
				if (debuggling) e.printStackTrace();
			}
			//*/

			// Check for and process expired donations.
			try {
				while (rx.next()) {
					if (rx.getString("expires") != null && rx.getString("expires") != "null" && rx.getString("expires").equalsIgnoreCase(getCurrentDate())) {
						String user = rx.getString("username");
						Double amount = rx.getDouble("amount");
						for (String pack : getConfig().getConfigurationSection("packages").getKeys(false)) {
							String price = getConfig().getString("packages." + pack + ".price");
							List<String> commands = getConfig().getStringList("packages." + pack + ".expires-commands");
							if (!getConfig().getBoolean("settings.cumulativepackages")) {
								if (amount.equals(price) || (amount + "0").equals(price)) {
									for (String cmnd : commands) {
										getServer().dispatchCommand(getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", amount + ""));
									}
								}
							} else {
								Double total = getTotalDonated(user);
								if (total.equals(price) || (total + "0").equals(price)) {
									for (String cmnd : commands) {
										getServer().dispatchCommand(getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", amount + ""));
									}
								}
							}
						}
						rx.updateString("expired", "true");
						rx.updateRow();
					}
				}
			} catch (Exception e) {
				log.severe("[Donator] Error encountered when checking for expired donations!");
				if (debuggling) e.printStackTrace();
			}
		}
	}

	public void checkForDonations() {
		if (!connectionfailed) {

			// Define Variables
			ResultSet r = getNewDonors();
			//ResultSet rx = getResultSet("SELECT * FROM donations WHERE expired='false'");

			// Check for and process new donations.
			try {
				while (r.next()) {
					String user = r.getString("username");
					Double amount = r.getDouble("amount");
					DonateEvent event = new DonateEvent(r.getString("username"), r.getDouble("amount"), r.getString("date"), r.getString("first_name"), r.getString("last_name"), r.getString("payer_email"), r.getString("expires"));
					getServer().getPluginManager().callEvent(event);
					r.updateString("processed", "true");
					r.updateRow();
					updateDonorPlayers(user, getTotalDonated(user) + amount);
				}
			} catch (Exception e) {
				log.severe("[Donator] Error encountered when checking for new donations!");
				if (debuggling) e.printStackTrace();
			}

			// Check for and process expired donations.
			/*
			try {
				while (rx.next()) {
					if (rx.getString("expires") != null && rx.getString("expires") != "null" && rx.getString("expires").equalsIgnoreCase(getCurrentDate())) {
						String user = rx.getString("username");
						Double amount = rx.getDouble("amount");
						for (String pack : getConfig().getConfigurationSection("packages").getKeys(false)) {
							String price = getConfig().getString("packages." + pack + ".price");
							List<String> commands = getConfig().getStringList("packages." + pack + ".expires-commands");
							if (!getConfig().getBoolean("settings.cumulativepackages")) {
								if (amount.equals(price) || (amount + "0").equals(price)) {
									for (String cmnd : commands) {
										getServer().dispatchCommand(getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", amount + ""));
									}
								}
							} else {
								Double total = getTotalDonated(user);
								if (total.equals(price) || (total + "0").equals(price)) {
									for (String cmnd : commands) {
										getServer().dispatchCommand(getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", amount + ""));
									}
								}
							}
						}
						rx.updateString("expired", "true");
						rx.updateRow();
					}
				}
			} catch (Exception e) {
				log.severe("[Donator] Error encountered when checking for expired donations!");
				if (debuggling) e.printStackTrace();
			}
			//*/
		}
	}

	// Connect to MySQL and create tables.
	public void setupMysql() {
		if (!connectionfailed) {
			try {
				Statement st = conn.createStatement();
				String table = "CREATE TABLE IF NOT EXISTS donations(id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), username TEXT(20), amount TEXT(5), date TEXT, processed TEXT, sandbox TEXT, first_name TEXT, last_name TEXT, payer_email TEXT, expires TEXT, expired TEXT, canGet INT)";
				String ptable = "CREATE TABLE IF NOT EXISTS players(username TEXT(20), amount TEXT(5))";
				st.executeUpdate(table);
				st.executeUpdate(ptable);
			} catch (final Exception e) {
				if (debuggling) e.printStackTrace();
			}
		}
	}

	public ResultSet getNewDonors() {
		return getResultSet("SELECT * FROM donations WHERE processed='false'");
	}

	public ResultSet getSetToExpireDonors() {
		return getResultSet("SELECT * FROM donations WHERE expired='false'");
	}

	public ResultSet getRecentDonors() {
		return getResultSet("SELECT * FROM donations ORDER BY id DESC LIMIT 5");
	}

	public ResultSet getDonationResult(String id) {
		return getResultSet("SELECT * FROM donations WHERE id='" + id +"'");
	}
	
	// vérifie si le joueur peut récupèrer ses items
	public boolean canGet(String user)
	{
		ResultSet r = getResultSet("SELECT * FROM donations WHERE username = '" + user + "' AND canGet = 1 AND expired='false'");
		try {
			if (r.next()) {
				if(r.getInt("canGet")>0)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
		return false;
	}
	
	// coupe l'accès a ces items
	public void setCanGet(String user)
	{
		executeRequete("UPDATE donations SET canGet=0 WHERE username='"+user+"'");
	}

	public Integer getDupes(String username, String amount) {
		ResultSet r = getResultSet("SELECT * FROM donations WHERE username = '" + username + "' AND amount = '" + amount + "'");
		Integer donationcount = 0;
		try {
			while (r.next()) {
				donationcount++;
			}
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
		return donationcount;
	}

	public void updateDonorPlayers(String user, Double amount) {
		try {
			Statement st = conn.createStatement();
			String sta = "REPLACE INTO players SET username = '" + user + "', amount = '" + amount +"'";
			st.executeUpdate(sta);
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
	}

	public String parseAmount(Double amount) {
		if (amount.toString().endsWith(".0")) {
			return "$" + amount + "0";
		}
		return "$" + amount;
	}

	public String parseAmount(String amount) {
		if (amount.toString().endsWith(".0")) {
			return "$" + amount + "0";
		}
		return "$" + amount;
	}

	// Calculate the total amount a player has donated.
	public Double getTotalDonated(String player) {
		ResultSet r = getResultSet("SELECT * FROM donations WHERE username='" + player + "'");
		Double am = null;
		if(r==null)
		{
			return new Double(0);
		}
		try {
			while (r.next()) {
				am = r.getDouble("amount");
			}
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
		if (am != null) {
			return am;
		} else {
			return new Double(0);
		}
	}
	
	// Calculate the total amount a player has donated.
	public Double getOnlyDonated(String player) {
		ResultSet r = getResultSet("SELECT * FROM donations WHERE username='" + player + "' AND expired='false'");
		Double am = null;
		try {
			while (r.next()) {
				am = r.getDouble("amount");
			}
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
		if (am != null) {
			return am;
		} else {
			return Double.parseDouble("0");
		}
	}

	// General method to get a result set.
	public ResultSet getResultSet(String statement) {
		ResultSet result = null;
		try {
			Statement st;
			st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (!getConfig().getBoolean("settings.sandbox")) {
				return st.executeQuery(statement);
			} else {
				return st.executeQuery(statement);
			}
		} catch (SQLException e) {
			if (debuggling) e.printStackTrace();
		}
		return result;
	}
	
	// General method to get a result set.
	public int executeRequete(String statement) {
			int result = -1;
			try {
				Statement st;
				st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				if (!getConfig().getBoolean("settings.sandbox")) {
					return st.executeUpdate(statement);
				} else {
					return st.executeUpdate(statement);
				}
			} catch (SQLException e) {
				if (debuggling) e.printStackTrace();
			}
			return result;
		}

	// Calculate the expires date for a package.
	public String getExpiresDate(String packagename) {
		Integer days = getConfig().getInt("packages." + packagename + ".expires");
		if (!(days == 0)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			try {
				c.setTime(sdf.parse(getCurrentDate()));
			} catch (ParseException e) {
				if (debuggling) e.printStackTrace();
			}
			c.add(Calendar.DATE, days);
			String exp = sdf.format(c.getTime());
			return exp;
		}
		return null;
	}

	public void updateSignWall(String use, Double amount) {
		Location loc1 = new Location(getServer().getWorld(getConfig().getString("signwall.1.w")), getConfig().getInt("signwall.1.x"), getConfig().getInt("signwall.1.y"), getConfig().getInt("signwall.1.z"));
		Location loc2 = new Location(getServer().getWorld(getConfig().getString("signwall.2.w")), getConfig().getInt("signwall.2.x"), getConfig().getInt("signwall.2.y"), getConfig().getInt("signwall.2.z"));
		Integer x1 = loc1.getBlockX(), y1 = loc1.getBlockY(), z1 = loc1.getBlockZ();
		Integer x2 = loc2.getBlockX(), y2 = loc2.getBlockY(), z2 = loc2.getBlockZ();
		Integer minx = Math.min(x1, x2);
		Integer minz = Math.min(z1, z2);
		Integer miny = Math.min(y1, y2);
		Integer maxx = Math.max(x1, x2);
		Integer maxy = Math.max(y1, y2), maxz = Math.max(z1, z2);
		List<String> li = getConfig().getStringList("signwall-format");
		for (Integer x = minx; x <= maxx; x++) for (Integer y = miny; y <= maxy; y++) for (Integer z = minz; z <= maxz; z++) {
			Block b = getServer().getWorld(getConfig().getString("signwall.2.w")).getBlockAt(x, y, z);
			Sign sign = (Sign)b.getState();
			if (sign.getLine(0).isEmpty() || sign.getLine(0) == null) {
				Integer currentline = -1;
				for (String line : li) {
					if (currentline < 5) {
						currentline++;
						sign.setLine(currentline, line.replace("%player", use).replace("%amount", parseAmount(amount)));
					}
				}
				sign.update();
				currentline = -1;
				return;
			}
		}
		return;
	}

	public String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public String colorize(String s){
		if(s == null) return null;
		return s.replaceAll("&([l-o0-9a-f])", "\u00A7$1");
	}

	public void formatRecentQuick(Player player, String id, String date, String username, String amount, String totalamount) {
		player.sendMessage("§8(#" + id + ") §6" + date.substring(9, date.length()).substring(0, 12) + " §a" + username + " §6"+m_translator.get("common",0)+" §a$" + amount + " §c(" + parseAmount(totalamount) + " "+m_translator.get("common",1)+")");
	}
	
	public void formatRecentQuick(CommandSender player, String id, String date, String username, String amount, String totalamount) {
		player.sendMessage("§8(#" + id + ") §6" + date.substring(9, date.length()).substring(0, 12) + " §a" + username + " §6"+m_translator.get("common",0)+" §a$" + amount + " §c(" + parseAmount(totalamount) + " "+m_translator.get("common",1)+")");
	}

	public void formatRecentByPlayer(Player player, String id, String date, String amount) {
		player.sendMessage("§8(#" + id + ") §6" + date.substring(9, date.length()).substring(0, 12) + " §6"+m_translator.get("common",0)+" §a$" + amount + "§6.");
	}
	
	public void formatRecentByPlayer(CommandSender player, String id, String date, String amount) {
		player.sendMessage("§8(#" + id + ") §6" + date.substring(9, date.length()).substring(0, 12) + " §6"+m_translator.get("common",0)+" §a$" + amount + "§6.");
	}

	public void noPermission(CommandSender cs) {
		cs.sendMessage("§8[Obole] §c"+m_translator.get("error",0)+"");
	}
	
	private boolean execOnlineCommand(CommandSender cs,String user)
	{
		//TODO une requette sql pour le montant de l'account
		boolean result=false;
		if(canGet(user))
		{
			result=onlineCommand(cs,user,getOnlyDonated(user));
			setCanGet(user);
		}
		return result;
	}
	
	private boolean onlineCommand(CommandSender cs,String user,Double amount)
	{
		boolean result=true;
		
		// Simple donation processing.

		for (String pack : this.getConfig().getConfigurationSection("packages").getKeys(false))
		{
			String price = this.getConfig().getString("packages." + pack + ".price");
			if(amount==Double.parseDouble(price))
			{
				List<String> commands = this.getConfig().getStringList("packages." + pack + ".onlineCommands");
				for (String cmnd : commands)
				{
					//cs.sendMessage(cmnd);
					this.getServer().dispatchCommand(this.getServer().getConsoleSender(), cmnd.replace("%player", user).replace("%amount", this.parseAmount(amount)));
				}
			}
		}

		
		return result;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
		
		if(m_translator==null)
		{
			m_translator=new Translator(getConfig().getString("settings.lng"));
		}
		
		if (cmnd.getName().equalsIgnoreCase("obole") || cmnd.getName().equalsIgnoreCase("ob")) {
			if (strings.length == 0) {
				cs.sendMessage("Test");
				ArrayList<String> message=m_translator.get("help");
				for(int t=0;t<message.size();t++)
				{
					cs.sendMessage(message.get(t));
				}
				
				
			}
			if (strings.length == 1) {
				if (strings[0].equalsIgnoreCase("recent")) {
					if (cs.hasPermission("obole.recent")) {
						cs.sendMessage(m_translator.get("recent",0));
						ResultSet r = getRecentDonors();
						try {
							while (r.next()) {
								formatRecentQuick( cs, r.getString("id"), r.getString("date"), r.getString("username"), r.getString("amount"), getTotalDonated(r.getString("username")) + "");
							}
						} catch (SQLException e) {
							if (debuggling) e.printStackTrace();
						}
					} else {
						noPermission(cs);
					}
				}
				if (strings[0].equalsIgnoreCase("get")) {
					if (cs.hasPermission("obole.get"))
					{
						ArrayList<String> message=m_translator.get("get");
						for(int t=0;t<message.size();t++)
						{
							cs.sendMessage(message.get(t));
						}
						

					} else {
						noPermission(cs);
					}
				}
				if (strings[0].equalsIgnoreCase("setwall")) {
					if (cs instanceof Player) {
						if (cs.hasPermission("obole.setwall")) {
							if (!settingwall.contains(cs.getName())) {
								settingwall.add(cs.getName());
								cs.sendMessage(m_translator.get("setWall",0));
								cs.sendMessage(m_translator.get("setWall",1));
							} else {
								settingwall.remove(cs.getName());
								cs.sendMessage(m_translator.get("setWall",0));
								cs.sendMessage(m_translator.get("setWall",2));
							}
						} else {
							noPermission(cs);
						}
					} else {
						cs.sendMessage("You can only do that as a player!");
					}
				}
				if (strings[0].equalsIgnoreCase("reload")) {
					if (cs.hasPermission("obole.reload")) {
						reloadConfig();
						saveConfig();
						m_translator=new Translator(getConfig().getString("settings.lng"));
						cs.sendMessage(m_translator.get("reload",0));
						cs.sendMessage(m_translator.get("reload",1));
					} else {
						noPermission(cs);
					}
				}
			}
			if (strings.length == 2) {
				if (strings[0].equalsIgnoreCase("check")) {
					if (cs.hasPermission("obole.check")) {
						ResultSet r = getDonationResult(strings[1]);
						cs.sendMessage("§8-------------- §6Donator §8- §aDonation # " + strings[1] + " §8--------------");
						try {
							while (r.next()) {
								cs.sendMessage(m_translator.get("check",0) + r.getString("username"));
								cs.sendMessage(m_translator.get("check",1) + r.getString("amount"));
								cs.sendMessage(m_translator.get("check",2) + r.getString("date").substring(9, r.getString("date").length()).substring(0, 12));
								cs.sendMessage(m_translator.get("check",3) + r.getString("first_name") + " " + r.getString("last_name"));
								cs.sendMessage(m_translator.get("check",4) + r.getString("payer_email"));
								cs.sendMessage(m_translator.get("check",5) + r.getString("expired").replace("false",m_translator.get("common",2)).replace("true", m_translator.get("common",3)));
								cs.sendMessage(m_translator.get("check",6) + r.getString("expires").replace("null", m_translator.get("common",4)));
							}
						} catch (SQLException e) {
							if (debuggling) e.printStackTrace();
						}
					} else {
						noPermission(cs);
					}
				}
				if (strings[0].equalsIgnoreCase("get") && strings[1].equalsIgnoreCase("accept")) {
					if (cs.hasPermission("obole.get.accept")) {
						if(execOnlineCommand(cs,cs.getName()))
						{
							cs.sendMessage(m_translator.get("getAccept",0));
						}
						else
						{
							cs.sendMessage(m_translator.get("getAccept",1));
							String temp=new String(m_translator.get("getAccept",2));
							this.getServer().broadcastMessage(this.colorize(temp.replace("%player", cs.getName())));
						}
					} else {
						noPermission(cs);
					}
				}
				if (strings[0].equalsIgnoreCase("checkplayer")) {
					if (cs.hasPermission("obole.checkplayer")) {
						cs.sendMessage(m_translator.get("checkPlayer",0) + strings[1] + " §8--------------");
						cs.sendMessage(m_translator.get("checkPlayer",1) + strings[1]);
						cs.sendMessage(m_translator.get("checkPlayer",2) + getTotalDonated(strings[1]));
						ResultSet r = getResultSet("SELECT * FROM donations WHERE username='" + strings[1] + "' ORDER BY id DESC LIMIT 5");
						try {
							while (r.next()) {
								formatRecentByPlayer( cs, r.getString("id"), r.getString("date"), r.getString("amount"));
							}
						} catch (SQLException e) {
							if (debuggling) e.printStackTrace();
						}
					} else {
						noPermission(cs);
					}
				}
			}
		}
		return true;
	}
}
