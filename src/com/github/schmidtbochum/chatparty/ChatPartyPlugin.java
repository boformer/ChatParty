/*
    ChatParty Plugin for Minecraft Bukkit Servers
    Copyright (C) 2013 Felix Schmidt
    
    This file is part of ChatParty.

    ChatParty is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ChatParty is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ChatParty.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.schmidtbochum.chatparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatPartyPlugin extends JavaPlugin
{
	private HashMap<String, Party> activeParties;
	private ArrayList<Player> spyPlayers;
	private boolean config_invertP;
	private boolean config_toggleWithP;
	public ChatColor config_messageColor;
	public String config_chatFormat;
	public final boolean GUILD_MODE = false;
	public final String TEXT_PARTY = (GUILD_MODE ? "guild" : "party");
	public final String TEXT_PARTY2 = (GUILD_MODE ? "Guild" : "Party");
	public final String TEXT_PARTIES = (GUILD_MODE ? "guilds" : "parties");
	public final String TEXT_P = (GUILD_MODE ? "g" : "p");
	
	public void onEnable()
	{
		// copy default config
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		config_invertP = getConfig().getBoolean("invertP");
		config_toggleWithP = getConfig().getBoolean("toggleWithP");
		config_chatFormat = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chatFormat"));
		config_messageColor = ChatColor.getByChar(getConfig().getString("messageColor").substring(1));
		if(config_messageColor == null) config_messageColor = ChatColor.WHITE;
		
		activeParties = new HashMap<String, Party>();
		spyPlayers = new ArrayList<Player>();
		
		for(Player player : getServer().getOnlinePlayers()) 
		{
			registerSpy(player);
		}
		
		
		getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);
	}
	
	public void onDisable()
	{
		//saveConfig();
	}
	
	public void saveParty(Party party) 
	{
		ConfigurationSection partySection = getConfig().getConfigurationSection("parties").createSection(party.name);
		partySection.set("leaders", party.leaders);
		partySection.set("members", party.members);
		saveConfig();
		reloadConfig();
	}
	
	public Party getPlayerParty(Player player) 
	{
		String partyName = getConfig().getConfigurationSection("players").getString(player.getName());
		if(partyName != null)
		{
			return loadParty(partyName);
		} 
		else 
		{
			return null;
		}
	}
	
	public void registerSpy(Player player) 
	{
		if(getConfig().getStringList("spy").contains(player.getName()))
		{
			spyPlayers.add(player);
		}
	}
	
	public void unregisterSpy(Player player) 
	{
		spyPlayers.remove(player);
	}
	
	public boolean toggleSpy(Player player) 
	{
		List<String> list = getConfig().getStringList("spy");
		boolean result;
		if(spyPlayers.contains(player)) 
		{
			spyPlayers.remove(player);
			list.remove(player.getName());
			result = false;
		}
		else
		{
			spyPlayers.add(player);
			list.add(player.getName());
			result = true;
		}
		getConfig().set("spy", list);
		saveConfig();
		return result;
	}
	
	private boolean togglePartyChat(Player player)
	{
		if(player.hasMetadata("partyToggle"))
		{
			player.removeMetadata("partyToggle", this);
			return false;
		}
		else
		{
			player.setMetadata("partyToggle", new FixedMetadataValue(this, true));
			return true;
		}
	}
	private boolean toggleGlobalChat(Player player)
	{
		if(player.hasMetadata("globalChatToggle"))
		{
			player.removeMetadata("globalChatToggle", this);
			return false;
		}
		else
		{
			player.setMetadata("globalChatToggle", new FixedMetadataValue(this, true));
			return true;
		}
	}
	
	public void sendSpyPartyMessage(Party party, String message) 
	{
		for(Player player : spyPlayers) 
		{
			if(player.hasPermission("chatparty.admin") && (!player.hasMetadata("party") || party.name != player.getMetadata("party").get(0).asString())) 
			{
				player.sendMessage(ChatColor.GRAY + "[" + party.shortName + "] " + message);
			}
		}
		getLogger().info("[" + party.shortName + "] " + message);
	}
	
	public void sendSpyChatMessage(Party party, Player sender, String message) 
	{
		sendSpyPartyMessage(party, sender.getName() +  ": " + message);
	}
	
	public Party loadParty(String name) 
	{
		Party party = activeParties.get(name);
		
		if(party == null) 
		{
			party = new Party(name, this);
			
			ConfigurationSection partySection = getConfig().getConfigurationSection("parties." + name);
			
			if(partySection == null || partySection.getStringList("leaders").size() == 0) return null;
			
			party.leaders = (ArrayList<String>) partySection.getStringList("leaders");
			party.members = (ArrayList<String>) partySection.getStringList("members");
			
			for(Player player : getServer().getOnlinePlayers()) 
			{
				if(party.leaders.contains(player.getName()) || party.members.contains(player.getName())) 
				{
					party.activePlayers.add(player);
				}
			}
		}
		
		return party;
	}
	
	private void disbandParty(Party party)
	{
		for(String playerName : party.members) 
		{
			removePlayer(playerName);
		}
		for(String playerName : party.leaders) 
		{
			removePlayer(playerName);
		}
		
		for(Player player : party.activePlayers) 
		{
			player.removeMetadata("party", this);
			player.removeMetadata("isPartyLeader", this);
		}
		
		activeParties.remove(party.name);
		
		party.leaders = null;
		party.members = null;
		
		//getConfig().getConfigurationSection("parties").set(party.name, null);
		
		
		
		saveConfig();
		
		this.getLogger().info("Disbanded the chat "+TEXT_PARTY+" \"" + party.name + "\".");
	}
	
	public void savePlayer(Player player) 
	{
		ConfigurationSection playerSection = getConfig().getConfigurationSection("players");

		if(!player.hasMetadata("party")) 
		{
			playerSection.set(player.getName(), null);
		}
		else
		{
			String partyName = player.getMetadata("party").get(0).asString();
			playerSection.set(player.getName(), partyName);
		}
		saveConfig();
	}
	
	public void removePlayer(String playerName) 
	{
		ConfigurationSection playerSection = getConfig().getConfigurationSection("players");
		playerSection.set(playerName, null);
		saveConfig();
	}
	
	public static Pattern ALPHANUMERIC = Pattern.compile("[A-Za-z0-9 ]+");
	
	private boolean validateName(String name) 
	{
		Matcher m = ALPHANUMERIC.matcher(name);
		return m.matches();
		
	}
	
	public void sendMessage(Player player, String message) 
	{
		player.sendMessage(config_messageColor + message);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) 
	{
		Player player = null;
		if (sender instanceof Player) 
		{
			player = (Player) sender;
		} 
		else 
		{
			return false;
		}
		if(cmd.getName().equalsIgnoreCase(TEXT_P)) 
		{
			//CONDITIONS
			
			if(!player.hasPermission("chatparty.user")) 
			{
				sendMessage(player, "You do not have access to that command.");
				return true;
			}
			if(!player.hasMetadata("party")) 
			{
				sendMessage(player, "You are not in a "+TEXT_PARTY+".");
				if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
				return true;
			}
			if(args.length == 0) 
			{
				if(!config_toggleWithP)
				{
					return false;
				}
				else
				{
					boolean enabled = togglePartyChat(player);
					
					if(enabled) 
					{
						sendMessage(player, "Toggled "+TEXT_PARTY+" chat.");
					}
					else
					{
						sendMessage(player, "Detoggled "+TEXT_PARTY+" chat.");
					}
					return true;
				}
			}
			
			//CONDITIONS END
			
			StringBuilder builder = new StringBuilder();
			for(String word : args) 
			{
			    if (builder.length() > 0) {
			        builder.append(" ");
			    }
			    builder.append(word);
			}
			
			String message = builder.toString();
			
			if(config_invertP && player.hasMetadata("partyToggle")) 
			{
				if(!player.hasMetadata("globalChatToggle"))
				{
					player.setMetadata("ignore", new FixedMetadataValue(this, true));
					player.chat(message);
					return true;
				}
				else
				{
					sendMessage(player, "Message cancelled. Type /chat to enable the global chat.");
					return true;
				}
			}
			
			String partyName = player.getMetadata("party").get(0).asString();
			Party party = loadParty(partyName);
			
			party.sendPlayerMessage(player, message);
			sendSpyChatMessage(party, player, message);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("chat")) 
		{
			//CONDITIONS
			
			if(!player.hasPermission("chatparty.user")) 
			{
				sendMessage(player, "You do not have access to that command.");
				return true;
			}
			//CONDITIONS END
			
			boolean toggled = toggleGlobalChat(player);
			
			if(toggled)
			{
				sendMessage(player, "The global chat is now hidden. Type /chat to enable the global chat.");
			}
			else {
				sendMessage(player, "The global chat is now visible.");
			}
			return true;
		}
		
		else if(cmd.getName().equalsIgnoreCase(TEXT_PARTY)) 
		{
			//CONDITIONS
			
			if(!player.hasPermission("chatparty.user")) 
			{
				sendMessage(player, "You do not have access to that command.");
				return true;
			}
			
			//CONDITIONS END
			
			if(args.length == 0 || args[0].equalsIgnoreCase("help")) 
			{
				sendMessage(player, "--- "+TEXT_PARTY2+" Help ---");
				
				sendMessage(player, "/chat" + ChatColor.WHITE + ": Toggle the public chat.");
				
				if(player.hasMetadata("party")) 
				{
					sendMessage(player, "/"+TEXT_P+" <message>" + ChatColor.WHITE + ": Send a message to your "+TEXT_PARTY+"");
					sendMessage(player, "/"+TEXT_PARTY+" leave" + ChatColor.WHITE + ": Leave your "+TEXT_PARTY+"");
					sendMessage(player, "/"+TEXT_PARTY+" members" + ChatColor.WHITE + ": Show the member list");
					sendMessage(player, "/"+TEXT_PARTY+" toggle" + ChatColor.WHITE + ": Toggle the "+TEXT_PARTY+" chat");
					if(player.hasMetadata("isPartyLeader") && player.hasPermission("chatparty.leader")) 
					{
						sendMessage(player, "/"+TEXT_PARTY+" invite <player>" + ChatColor.WHITE + ": Invite a player to your "+TEXT_PARTY+"");
						sendMessage(player, "/"+TEXT_PARTY+" kick <player>" + ChatColor.WHITE + ": Kick a player from your "+TEXT_PARTY+"");
						//sendMessage(player, "/party name <name>" + ChatColor.WHITE + ": Rename your party.");
						sendMessage(player, "/"+TEXT_PARTY+" leader <player>" + ChatColor.WHITE + ": Add a leader to your "+TEXT_PARTY+"");
					}
				}
				else
				{
					sendMessage(player, "/"+TEXT_PARTY+" join" + ChatColor.WHITE + ": Accept a "+TEXT_PARTY+" invitation");
					if(player.hasPermission("chatparty.leader"))
					{
						sendMessage(player, "/"+TEXT_PARTY+" create <name>" + ChatColor.WHITE + ": Create a new chat "+TEXT_PARTY+"");
					}
				}
				if(player.hasPermission("chatparty.admin"))
				{
					sendMessage(player, "/"+TEXT_PARTY+" spy" + ChatColor.WHITE + ": Toggle messages from all "+TEXT_PARTIES+".");
				}
				return true;
			} 
			else if(args[0].equalsIgnoreCase("join")) 
			{
				//CONDITIONS
				
				if(!player.hasMetadata("partyInvitation")) 
				{
					sendMessage(player, "No active "+TEXT_PARTY+" invitation.");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
					
				String partyName = player.getMetadata("partyInvitation").get(0).asString();
				Party party = loadParty(partyName);
				
				if(party == null) 
				{
					sendMessage(player, "No active "+TEXT_PARTY+" invitation.");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
					return true;
				} 
				
				//CONDITIONS END
				
				player.removeMetadata("partyInvitation", this);
				
				party.sendPartyMessage(player.getDisplayName() + ChatColor.GREEN + " joined the "+TEXT_PARTY+".");
				sendSpyPartyMessage(party, player.getName() + " joined the "+TEXT_PARTY+".");
				
				party.members.add(player.getName());
				party.activePlayers.add(player);
				
				player.setMetadata("party", new FixedMetadataValue(this, party.name));
				
				sendMessage(player, "You joined the "+TEXT_PARTY+" \"" +  party.name +"\".");
				sendMessage(player, "Chat with /"+TEXT_P+" <message>");
				
				savePlayer(player);
				saveParty(party);
				return true;
			}
			else if(args[0].equalsIgnoreCase("leave")) 
			{
				//CONDITIONS
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a "+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				//CONDITIONS END
				
				String partyName = player.getMetadata("party").get(0).asString();
				player.removeMetadata("party", this);
				player.removeMetadata("isPartyLeader", this);
				
				Party party = loadParty(partyName);
				
				party.leaders.remove(player.getName());
				party.members.remove(player.getName());
				party.activePlayers.remove(player);
				
				removePlayer(player.getName());
				
				if(party.leaders.size() == 0) 
				{
					party.sendPartyMessage("The "+TEXT_PARTY+" was disbanded because all leaders left.");
					sendSpyPartyMessage(party, "The "+TEXT_PARTY+" was disbanded.");
					disbandParty(party);
				}
				
				
				party.sendPartyMessage(player.getDisplayName() + ChatColor.GREEN + " left the "+TEXT_PARTY+".");
				sendSpyPartyMessage(party, player.getName() + " left the "+TEXT_PARTY+".");
				
				sendMessage(player, "You left the "+TEXT_PARTY+" \"" +  party.name +"\".");
				
				savePlayer(player);
				saveParty(party);
				return true;
			}
			else if(args[0].equalsIgnoreCase("invite")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.leader")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a "+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				if(!player.hasMetadata("isPartyLeader")) 
				{
					sendMessage(player, "Only "+TEXT_PARTY+" leaders can invite other players.");
					return true;
				}
				
				if(args.length != 2) 
				{
					sendMessage(player, "Usage: /"+TEXT_PARTY+" invite <player>");
					return true;
				}
				
				String playerName = args[1];
				Player invitedPlayer = getServer().getPlayer(playerName);
				
				if(invitedPlayer == null || !invitedPlayer.isOnline()) 
				{
					sendMessage(player, "You can only invite online players.");
					return true;
				}
				
				if(!invitedPlayer.hasPermission("chatparty.user"))
				{
					sendMessage(player, "The player does not have the permission for the "+TEXT_PARTY+" system.");
					return true;
				}
				
				if(invitedPlayer.hasMetadata("party"))
				{
					sendMessage(player, "The player is already in a "+TEXT_PARTY+".");
					return true;
				}
				
				//CONDITIONS END
				
				String partyName = player.getMetadata("party").get(0).asString();
				Party party = loadParty(partyName);
				
				invitedPlayer.setMetadata("partyInvitation", new FixedMetadataValue(this, party.name));
				
				sendMessage(player, "You invited " +  invitedPlayer.getName() + " to your "+TEXT_PARTY+".");
				
				sendMessage(invitedPlayer, player.getName() + " invited you to the "+TEXT_PARTY+" \"" + party.name + "\".");
				sendMessage(invitedPlayer, "To accept the invitation, type /"+TEXT_PARTY+" join");
				return true;
			}
			else if(args[0].equalsIgnoreCase("create")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.leader")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(player.hasMetadata("party")) 
				{
					sendMessage(player, "You are already in a "+TEXT_PARTY+".");
					return true;
				}
				
				if(args.length != 2) 
				{
					sendMessage(player, "Usage: /"+TEXT_PARTY+" create <name>");
					return true;
				}
				
				String partyName = args[1];
				
				if(partyName.length() > 15) 
				{
					sendMessage(player, "This name is too long! (3-15 letters)");
					return true;
				}
				if(partyName.length() < 3) 
				{
					sendMessage(player, "This name is too short! (3-15 letters)");
					return true;
				}
				
				if(!validateName(partyName)) {
					sendMessage(player, "\"" + partyName + "\" is not a valid name. Allowed characters are A-Z, a-z, 0-9.");
					return true;
				}
				
				if(loadParty(partyName) != null) {
					sendMessage(player, "The party \"" + partyName + "\" already exists. Please choose a different name.");
					return true;
				}
				
				//CONDITIONS END
				
				Party party = new Party(partyName, this);
				
				party.leaders.add(player.getName());
				party.activePlayers.add(player);
				
				player.setMetadata("party", new FixedMetadataValue(this, party.name));
				player.setMetadata("isPartyLeader", new FixedMetadataValue(this, true));
				
				activeParties.put(partyName, party);
				
				savePlayer(player);
				saveParty(party);
				
				sendMessage(player, "You created the "+TEXT_PARTY+" \"" + party.name + "\".");
				sendMessage(player, "Invite your friends with /"+TEXT_PARTY+" invite <player>");
				sendMessage(player, "Send a message to your "+TEXT_PARTY+" with /"+TEXT_P+" <message>");
				
				this.getLogger().info("Created the chat "+TEXT_PARTY+" \"" + party.name + "\".");
				
				return true;
			}
			else if(args[0].equalsIgnoreCase("leader")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.leader")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a "+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own party with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				if(!player.hasMetadata("isPartyLeader")) 
				{
					sendMessage(player, "Only "+TEXT_PARTY+" leaders can promote other players.");
					return true;
				}
				
				if(args.length != 2) 
				{
					sendMessage(player, "Usage: /"+TEXT_PARTY+" leader <player>");
					return true;
				}
				
				String playerName = args[1];
				OfflinePlayer promotedPlayer = getServer().getOfflinePlayer(playerName);
				
				String partyName = player.getMetadata("party").get(0).asString();
				Party party = loadParty(partyName);
				
				if(party.leaders.contains(promotedPlayer.getName())) 
				{
					sendMessage(player, "The player is already a leader.");
					return true;
				}
				
				if(!party.members.contains(promotedPlayer.getName()))
				{
					sendMessage(player, "The player is not a member of your "+TEXT_PARTY+".");
					return true;
				}
				
				//CONDITIONS END
				
				party.members.remove(promotedPlayer.getName());
				party.leaders.add(promotedPlayer.getName());
				
				
				Player onlinePlayer = getServer().getPlayer(playerName);
				
				if(onlinePlayer != null && onlinePlayer.isOnline())
				{
					onlinePlayer.setMetadata("isPartyLeader", new FixedMetadataValue(this, true));
				}
				saveParty(party);
				
				party.sendPartyMessage(promotedPlayer.getName() + ChatColor.GREEN + " is now a leader of the "+TEXT_PARTY+".");
				sendSpyPartyMessage(party, promotedPlayer.getName() + " is now a leader of the "+TEXT_PARTY+".");
				
				return true;
			}
			else if(args[0].equalsIgnoreCase("kick")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.leader")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a "+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own party with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				if(!player.hasMetadata("isPartyLeader")) 
				{
					sendMessage(player, "Only "+TEXT_PARTY+" leaders can kick other players.");
					return true;
				}
				
				if(args.length != 2) 
				{
					sendMessage(player, "Usage: /"+TEXT_PARTY+" kick <player>");
					return true;
				}
				
				String playerName = args[1];
				OfflinePlayer kickedPlayer = getServer().getOfflinePlayer(playerName);
				
				String partyName = player.getMetadata("party").get(0).asString();
				Party party = loadParty(partyName);
				
			
				if(party.leaders.contains(kickedPlayer.getName())) 
				{
					sendMessage(player, "You can't kick "+TEXT_PARTY+" leaders.");
					return true;
				}
				
				if(!party.members.contains(kickedPlayer.getName()))
				{
					sendMessage(player, "The player is not a member of your "+TEXT_PARTY+".");
					return true;
				}
				
				//CONDITIONS END
				
				party.members.remove(kickedPlayer.getName());
				
				Player onlinePlayer = getServer().getPlayer(playerName);
				if(onlinePlayer != null)
				{
					onlinePlayer.removeMetadata("party", this);
					party.activePlayers.remove(onlinePlayer);
					sendMessage(onlinePlayer, "You were kicked from the "+TEXT_PARTY+" \"" + party.name + "\".");
				}
				
				removePlayer(kickedPlayer.getName());
				saveParty(party);
				
				party.sendPartyMessage(kickedPlayer.getName() + " was kicked from the "+TEXT_PARTY+".");
				sendSpyPartyMessage(party, kickedPlayer.getName() + " was kicked from the "+TEXT_PARTY+".");

				return true;
			}
			else if(args[0].equalsIgnoreCase("members")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.user")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a"+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own party with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				//CONDITIONS END
				
				String partyName = player.getMetadata("party").get(0).asString();
				Party party = loadParty(partyName);
				
				String sep = ", ";
				
				StringBuilder builder = new StringBuilder();
				for(String name : party.leaders) 
				{
				    if (builder.length() > 0) {
				        builder.append(sep);
				    }
				    builder.append(name);
				}
				
				String leaders = builder.toString();
				
				builder = new StringBuilder();
				for(String name : party.members) 
				{
				    if (builder.length() > 0) {
				        builder.append(sep);
				    }
				    builder.append(name);
				}
				
				String members = builder.toString();
				
				sendMessage(player, "Member List of the "+TEXT_PARTY+" \"" + party.name + "\":");
				sendMessage(player, "Leaders (" + party.leaders.size() + "): " +  leaders);
				sendMessage(player, "Members (" + party.members.size() + "): " +  members);
				
				return true;
			}
			else if(args[0].equalsIgnoreCase("spy")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.admin")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				//CONDITIONS END
				
				boolean enabled = toggleSpy(player);
				
				if(enabled) 
				{
					sendMessage(player, "You enabled the spy mode.");
				}
				else
				{
					sendMessage(player, "You disabled the spy mode.");
				}
				
				return true;
			}
			else if(args[0].equalsIgnoreCase("toggle")) 
			{
				//CONDITIONS
				
				if(!player.hasPermission("chatparty.user")) 
				{
					sendMessage(player, "You do not have access to that command.");
					return true;
				}
				
				if(!player.hasMetadata("party")) 
				{
					sendMessage(player, "You are not in a "+TEXT_PARTY+".");
					if(player.hasPermission("chatparty.leader")) sendMessage(player, "Create your own "+TEXT_PARTY+" with /"+TEXT_PARTY+" create <name>.");
					return true;
				}
				
				//CONDITIONS END
				
				boolean enabled = togglePartyChat(player);
				
				if(enabled) 
				{
					sendMessage(player, "Toggled "+TEXT_PARTY+" chat.");
				}
				else
				{
					sendMessage(player, "Detoggled "+TEXT_PARTY+" chat.");
				}
				
				return true;
			}
			
		}
		return false;
	}
}
