/*
 ChatParty Plugin for Minecraft Bukkit Servers
 This file: Copyright (C) 2013-2014 Anthony Som
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

package supercheese200.NSFWChat;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles the NSFW channel. 
 */
public class NSFWChat {

    private final ChatPartyPlugin plugin;

    public NSFWChat(ChatPartyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a message to the NSFW chat channel
     *
     * @param sender The sender of the message, or null if sent from Console
     * @param message The message to send
     */
    public void sendNSFWMessage(Player sender, String message) {
        String tag = "*Console*";
        if (sender != null) {
            tag = sender.getDisplayName();
        }

        String formattedMessage = plugin.getNSFWChatTemplate().replace("{DISPLAYNAME}", tag).replace("{MESSAGE}", message);
        for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
            if (pla.hasMetadata("nsfwlistening")) {
                pla.sendMessage(formattedMessage);
            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage(formattedMessage);
    }
}
