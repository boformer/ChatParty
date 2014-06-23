/*
 ChatParty Plugin for Minecraft Bukkit Servers
 This file: Copyright (C) 2013-2014 Anthony Som
 Portions copyright (c) 2014 Dr Daniel Naylor
    
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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;

/**
 * A class that represents the /listennsfw command.
 */
public class NSFWListenCommand extends BaseCommandExecutor {

    public NSFWListenCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /listennsfw command
     *
     * @param sender The sender of this command
     * @param cmd The command class
     * @param label The actual command typed by the user
     * @param args The arguments of the command
     * @return <code>true</code>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("The console cannot toggle NSFW Chat channel");
            return true;
        }
        
        Player pla = (Player) sender;
        if (plugin.toggleNSFWListening(pla)) {
            plugin.sendMessage(pla, "NSFW Chat WILL be displayed from now on.");
        } else {
            plugin.sendMessage(pla, "NSFW Chat WILL NOT be displayed from now on.");
        }

        return true;
    }

}
