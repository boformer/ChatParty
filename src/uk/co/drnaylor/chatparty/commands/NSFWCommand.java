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
 * A class that represents the /nsfw command.
 */
public class NSFWCommand extends BaseCommandExecutor {

    public NSFWCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /nsfw command.
     *
     * @param sender The sender of the command
     * @param cmd The command class
     * @param label The actual string the user entered
     * @param args The arguments of the command
     * @return <code>true</code>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {

        // If we have arguements, then the command should be sent to the 
        // NSFW channel directly.
        if (args.length >= 1) {
            directToNSFWChat(this.getPlayerFromSender(sender), args);
        } else {
            // We can only execute this if this is player. If not, all over.
            Player pla = getPlayerFromSender(sender);
            if (pla == null) {
                sender.sendMessage("You cannot toggle NSFW Chat from console, please use /nsfwchat <msg>");
                return true;
            }
            
            if (plugin.toggleNSFWChat(pla)) {
                // If the player is not listening to the NSFW chat, auto toggle it on.
                if (pla.hasMetadata("nsfwlistening")) {
                    plugin.sendMessage(pla, "NSFW Chat is ON");
                } else {
                    plugin.toggleNSFWListening(pla);
                    plugin.sendMessage(pla, "NSFW Chat is ON - listening has been enabled.");
                }
            } else {
                plugin.sendMessage(pla, "NSFW Chat is OFF");
            }
        }
        return true;
    }

    private void directToNSFWChat(Player player, String[] args) {
        if (player != null && !player.hasMetadata("nsfwlistening")) {
            // The player should be told they are not listening.
            plugin.sendMessage(player, "You cannot send a message to the NSFW channel if you are not listening to it!");
            return;
        }
        
        StringBuilder s = new StringBuilder();
        for (String st : args) {
            if (s.length() > 0) {
                s.append(" ");
            }

            s.append(st);
        }

        plugin.getNSFWChat().sendNSFWMessage(player, s.toString());
    }
}
