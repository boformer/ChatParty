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
package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand extends BaseCommandExecutor {

    public ChatCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
        Player player = getPlayerFromSender(sender);
        if (player == null) {
            return false;
        }

        if (!player.hasPermission("chatparty.user")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return true;
        }
        //CONDITIONS END

        boolean toggled = plugin.toggleGlobalChat(player);

        if (toggled) {
            plugin.sendMessage(player, "The global chat is now hidden. Type /chat to enable the global chat.");
        } else {
            plugin.sendMessage(player, "The global chat is now visible.");
        }
        return true;
    }

}
