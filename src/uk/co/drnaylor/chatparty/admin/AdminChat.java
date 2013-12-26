/*
 ChatParty Plugin for Minecraft Bukkit Servers
 Copyright (C) 2013 Felix Schmidt
 Portions copyright (c) 2013-2014 Dr Daniel Naylor
    
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

package uk.co.drnaylor.chatparty.admin;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import org.bukkit.entity.Player;


public class AdminChat {

    private final ChatPartyPlugin plugin;
    
    public AdminChat(ChatPartyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sends a message to the admin chat.
     * 
     * @param sender The sender of the message, or null if console.
     * @param message The message.
     */
    public void sendAdminMessage(Player sender, String message) {
        String tag = "*Console*";
        if (sender != null) {
            tag = sender.getDisplayName();
        }
        
        String formattedMessage = plugin.getAdminChatTemplate().replace("{DISPLAYNAME}", tag).replace("{MESSAGE}", message);
        plugin.getServer().broadcast(formattedMessage, "chatparty.adminchat");
    }
    
}
