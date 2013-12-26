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

/**
 * A class that represents the /a command.
 */
public class ACommand extends BaseCommandExecutor {

    public ACommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /a command.
     * 
     * @param cs The sender of the command
     * @param cmnd The command this represents
     * @param string The actual command used
     * @param args The arguments to the command.
     * @return <code>true</code> if successful.
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if (args.length == 0) {
            Player player = this.getPlayerFromSender(cs);
            if (player == null) {
                return false;
            }

            toggleAdminChat(player);
        } else {
            directToAdminChat(this.getPlayerFromSender(cs), args);
        }
        
        return true;
    }
    
    private void toggleAdminChat(Player player) {
        boolean isOn = plugin.toggleAdminChat(player);
        if (isOn) {
            plugin.sendMessage(player, "Admin chat is ON");
        } else {
            plugin.sendMessage(player, "Admin chat is OFF");
        }
    }
    
    private void directToAdminChat(Player player, String[] args) {
        StringBuilder s = new StringBuilder();
        for (String st : args) {
            if (st.length() > 0) {
                s.append(" ");
            }
            
            s.append(st);
        }
        
        plugin.getAdminChat().sendAdminMessage(player, s.toString());
    }
}
