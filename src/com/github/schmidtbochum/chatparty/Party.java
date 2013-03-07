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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Party
{
	public String name;
	public String shortName;
	public ArrayList<String> members;
	public ArrayList<String> leaders;
	
	public ArrayList<Player> activePlayers;
	
	public Party(String name)
	{
		this.name = name;
		this.shortName = name.substring(0, 3);
		
		members = new ArrayList<String>();
		leaders = new ArrayList<String>();
		activePlayers = new ArrayList<Player>();
	}
	
	public void sendPlayerMessage(Player sender, String message) 
	{
		for(Player player : activePlayers) 
		{
			if(player.hasPermission("chatparty.user")) 
			{
				player.sendMessage(ChatColor.GREEN + "[P] " + ChatColor.WHITE + sender.getDisplayName() + ChatColor.WHITE + ": " + message);
			}
		}
	}
	public void sendPartyMessage(String message) 
	{
		for(Player player : activePlayers) 
		{
			if(player.hasPermission("chatparty.user")) 
			{
				player.sendMessage(ChatColor.GREEN + "[Party] " + message);
			}
		}
	}
}
