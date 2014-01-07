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
import com.github.schmidtbochum.chatparty.Party;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Executor for the /p command
 */
public class PCommand extends BaseCommandExecutor {

    /**
     * Initialises the command class.
     * 
     * @param plugin The plugin this command is attached to.
     */
    public PCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        Player player = super.getPlayerFromSender(cs);
        if (player == null) {
            return false;
        }

        // Start the conditions. Does the player have the right permissions?
        if (!player.hasPermission("chatparty.user")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return true;
        }
        
        // Is the player in a party?
        if (!player.hasMetadata("party")) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }
            return true;
        }
        
        // Are there any arguments to the command?
        if (args.length == 0) {
            
            // If the command allows for a toggle, then 
            if (!plugin.getToggleWithP()) {
                return false;
            } else {
                boolean enabled = plugin.togglePartyChat(player);

                if (enabled) {
                    plugin.sendMessage(player, "Party chat is now ON.");
                } else {
                    plugin.sendMessage(player, "Party chat is now OFF.");
                }
                return true;
            }
        }

        //CONDITIONS END
        StringBuilder builder = new StringBuilder();
        for (String word : args) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(word);
        }

        String message = builder.toString();

        if (plugin.getInvertP() && player.hasMetadata("partyToggle")) {
            if (!player.hasMetadata("globalChatToggle")) {
                player.setMetadata("ignore", new FixedMetadataValue(plugin, true));
                player.chat(message);
                return true;
            } else {
                plugin.sendMessage(player, "Message cancelled. Type /chat to enable the global chat.");
                return true;
            }
        }

        String partyName = player.getMetadata("party").get(0).asString();
        Party party = plugin.loadParty(partyName);

        party.sendPlayerMessage(player, message);
        return true;
    }

}
