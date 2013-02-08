package me.monnierant.obole;

import java.util.ArrayList;

public class Translator
{
	// la liste des textes séparer en sections
	private ArrayList<ArrayList<String>> m_texts;

	//le nom des sections
	private ArrayList<String> m_sections;

	public Translator(String _lng)
	{
		if (_lng == null)
		{
			_lng = "EN";
		}
		// le numéro de la langue
		int numLng = 0;
		ArrayList<String> lng = new ArrayList<String>();
		lng.add("FR");
		lng.add("EN");

		for (int t = 0; t < lng.size(); t++)
		{
			if (_lng.equals(lng.get(t)))
			{
				numLng = t;
			}
		}

		m_sections = new ArrayList<String>();
		m_sections.add("commons");
		m_sections.add("error");
		m_sections.add("help");
		m_sections.add("recent");
		m_sections.add("get");
		m_sections.add("setWall");
		m_sections.add("reload");
		m_sections.add("check");
		m_sections.add("getAccept");
		m_sections.add("checkPlayer");

		m_texts = new ArrayList<ArrayList<String>>();

		switch (numLng)
		{
		// FR
		case 0:
			m_texts.add(new ArrayList<String>());
			m_texts.get(0).add("pour");
			m_texts.get(0).add("Total");
			m_texts.get(0).add("Non");
			m_texts.get(0).add("Oui");
			m_texts.get(0).add("Jamais");
			m_texts.add(new ArrayList<String>());
			m_texts.get(1).add("Vous n'avez pas les droit pour effectuer cette opération.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(2).add("§8-------------- §6Obole §8- §aInfos Générales. §8--------------");
			m_texts.get(2).add("§7Accepte les dons et donne automatiquement les cadeaux en jeux!");
			m_texts.get(2).add("§7Plugin développer par §9Monnierant§7.");
			m_texts.get(2).add("§6/obole recent §b dons recents");
			m_texts.get(2).add("§6/obole get §b obtenir les cadeaux");
			m_texts.get(2).add("§6/obole setwall §b obtenir l'outil de selection du mur des donateurs");
			m_texts.get(2).add("§6/obole check <id> §bObtenir les details d'un don");
			m_texts.get(2).add("§6/obole checkplayer <player> §bVoir le montant des dons du joueur");
			m_texts.get(2).add("§6/obole reload §bRecharge la configuration");
			m_texts.add(new ArrayList<String>());
			m_texts.get(3).add("§8-------------- §6Obole §8- §aRecent Donations §8--------------");
			m_texts.add(new ArrayList<String>());
			m_texts.get(4).add("§8-------------- §6Obole §8- §aGet Info. §8--------------");
			m_texts.get(4).add("§7Veillez a avoir suffisament de place dans vottre inventaire avant d'effectuer la commande:");
			m_texts.get(4).add("§6/obole get accept §b pour obtenir vos cadeaux.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(5).add("§8-------------- §6Obole §8- §aSign Wall §8--------------");
			m_texts.get(5).add("§8Wand activer, utiliser la hache en or pour définir le mur des signs");
			m_texts.get(5).add("§8Wand desactiver.");
			m_texts.get(5).add("§8Point un bien défini");
			m_texts.get(5).add("§8Point deux bien défini");
			m_texts.add(new ArrayList<String>());
			m_texts.get(6).add("§8-------------- §6Obole §8- §aConfiguration §8--------------");
			m_texts.get(6).add("§8Configuration recharger avec succès.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(7).add("§6Nom du joueur: §c");
			m_texts.get(7).add("§6Montant donner: §a$");
			m_texts.get(7).add("§6Date du don: §7");
			m_texts.get(7).add("§6Nom complet: §7");
			m_texts.get(7).add("§6Email: §7");
			m_texts.get(7).add("§6Est expirè: §7");
			m_texts.get(7).add("§6Expire le: §7");
			m_texts.add(new ArrayList<String>());
			m_texts.get(8).add("§6Merci beaucoup pour votre aide");
			m_texts.get(8).add("§6Bien jouer petit malin");
			m_texts.get(8).add("§8%player §7à essayé de jouer au petit malin pour obtenir plusieur fois ces cadeaux");
			m_texts.add(new ArrayList<String>());
			m_texts.get(9).add("§8-------------- §6Obole §8- §aJoueur ");
			m_texts.get(9).add("§6Joueur: §c");
			m_texts.get(9).add("§6Total Donné: §a$");

			break;
		// EN
		case 1:
		default:
			m_texts.add(new ArrayList<String>());
			m_texts.get(0).add("for");
			m_texts.get(0).add("Total");
			m_texts.get(0).add("No");
			m_texts.get(0).add("Yes");
			m_texts.get(0).add("Never");
			m_texts.add(new ArrayList<String>());
			m_texts.get(1).add("You do not have permission to do that.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(2).add("§8-------------- §6Obole §8- §aGeneral Info. §8--------------");
			m_texts.get(2).add("§7Accept donations and automatically give ingame perks!");
			m_texts.get(2).add("§7Plugin developed by §9Monnierant§7.");
			m_texts.get(2).add("§6/obole recent §b Recent don");
			m_texts.get(2).add("§6/obole get §b Get gifts");
			m_texts.get(2).add("§6/obole setwall §b Get tool for setting wall");
			m_texts.get(2).add("§6/obole check <id> §bGet info about a donation");
			m_texts.get(2).add("§6/obole checkplayer <player> §bGet info about a player's donation");
			m_texts.get(2).add("§6/obole reload §bReload configuration");
			m_texts.add(new ArrayList<String>());
			m_texts.get(3).add("§8-------------- §6Obole §8- §aRecent Donations §8--------------");
			m_texts.add(new ArrayList<String>());
			m_texts.get(4).add("§8-------------- §6Obole §8- §aGet Info. §8--------------");
			m_texts.get(4).add("§7You must have enougth place in your inventory before using:");
			m_texts.get(4).add("§6/obole get accept §b in order to obtain gifts");
			m_texts.add(new ArrayList<String>());
			m_texts.get(5).add("§8-------------- §6Obole §8- §aSign Wall §8--------------");
			m_texts.get(5).add("§8Wand enabled, please use a golden axe to set the sign wall.");
			m_texts.get(5).add("§8Wand disabled.");
			m_texts.get(5).add("§8Point one successfully set.");
			m_texts.get(5).add("§8Point two successfully set.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(6).add("§8-------------- §6Obole §8- §aConfiguration §8--------------");
			m_texts.get(6).add("§8Successfully reloaded the configuration.");
			m_texts.add(new ArrayList<String>());
			m_texts.get(7).add("§6Player Name: §c");
			m_texts.get(7).add("§6Amount Donated: §a$");
			m_texts.get(7).add("§6Date Donated: §7");
			m_texts.get(7).add("§6Full Name: §7");
			m_texts.get(7).add("§6Email: §7");
			m_texts.get(7).add("§6Has Expired: §7");
			m_texts.get(7).add("§6Expires On: §7");
			m_texts.add(new ArrayList<String>());
			m_texts.get(8).add("§6Thank's a lot for your help");
			m_texts.get(8).add("§6Well tried young padawan");
			m_texts.get(8).add("§8%player §7try to cheat the gift");
			m_texts.add(new ArrayList<String>());
			m_texts.get(9).add("§8-------------- §6Obole §8- §aPlayer ");
			m_texts.get(9).add("§6Player: §c");
			m_texts.get(9).add("§6Total Donated: §a$");

			break;
		}

	}

	public ArrayList<String> get(String _sections)
	{

		for (int t = 0; t < m_sections.size(); t++)
		{
			if (m_sections.get(t).equals(_sections))
			{
				return m_texts.get(t);
			}
		}

		return new ArrayList<String>();
	}

	public String get(String _sections, int _num)
	{

		for (int t = 0; t < m_sections.size(); t++)
		{
			if (m_sections.get(t).equals(_sections))
			{
				if (m_texts.get(t).size() > _num && _num >= 0)
				{
					return m_texts.get(t).get(_num);
				}
				else
				{
					return "";
				}
			}
		}

		return "";
	}

}
