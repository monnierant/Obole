package me.monnierant.obole;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class Tracer
{
	public enum eNiveau
	{
		INFO, WARNING, SEVERE
	}

	private ConsoleCommandSender m_console;

	private Logger m_log;

	private String m_writerFile;

	private int m_defaultParam;

	private String m_permission;

	private final int indexFichier = 1;

	private final int indexLog = 2;

	private final int indexConsole = 4;

	private final int indexPlayer = 8;

	public Tracer(String chemin, ConsoleCommandSender _console, int _defaultParam, String _permission)
	{
		m_writerFile = chemin;
		m_log = Logger.getLogger("Minecraft");
		m_console = _console;
		m_defaultParam = _defaultParam;
		m_permission = _permission;
	}

	public void close()
	{

	}

	public String getNiveau(eNiveau niveau)
	{
		String nivString = "";
		switch (niveau)
		{
		case INFO:
			nivString = "info";
			break;
		case WARNING:
			nivString = "warning";
			break;
		case SEVERE:
			nivString = "severe";
			break;
		default:
			nivString = "info";
			break;
		}
		return nivString;
	}

	public boolean isSet(int test, int categorie)
	{
		return (0x1 & (test >> categorie)) == 1 ? true : false;
	}

	public void log(eNiveau niveau, String _message, CommandSender _player)
	{
		log(niveau, _message, _player, m_defaultParam);
	}

	public void log(eNiveau niveau, String _message, CommandSender _player, int _param)
	{
		if (isSet(m_defaultParam, indexFichier))
		{
			try
			{
				FileWriter writer = new FileWriter(m_writerFile, true);

				if (writer != null)
				{

					writer.write("[" + getNiveau(niveau) + "]" + _message + System.getProperty("line.separator"));
					writer.close();
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (isSet(m_defaultParam, indexLog))
		{
			switch (niveau)
			{
			case INFO:
				m_log.info(_message);
				break;
			case WARNING:
				m_log.warning(_message);
				break;
			case SEVERE:
				m_log.severe(_message);
				break;
			default:
				m_log.info(_message);
				break;
			}
		}
		else if (isSet(m_defaultParam, indexConsole))
		{
			m_console.sendMessage("[" + getNiveau(niveau) + "]" + _message);
		}
		else if (isSet(m_defaultParam, indexPlayer))
		{
			if (_player != null)
			{
				if (_player.hasPermission(m_permission))
				{
					_player.sendMessage("[" + getNiveau(niveau) + "]" + _message);
				}
			}
		}
	}

	public void updateParam(String chemin, int _defaultParam)
	{
		m_writerFile = chemin;

		m_defaultParam = _defaultParam;
	}

}
