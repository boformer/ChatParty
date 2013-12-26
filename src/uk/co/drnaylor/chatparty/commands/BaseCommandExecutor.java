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
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommandExecutor implements CommandExecutor {
    
    protected final ChatPartyPlugin plugin;
        
    /**
     * Constructs the object, and provides a reference to the plugin.
     * 
     * @param plugin The plugin.
     */
    protected BaseCommandExecutor(ChatPartyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Gets the player object from the sender, should the sender be a player.
     * 
     * @param sender The sender of the command.
     * @return The player, or <code>null</code> if the sender is the console.
     */
    protected Player getPlayerFromSender(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        } 
        return null;
    }
    
}
