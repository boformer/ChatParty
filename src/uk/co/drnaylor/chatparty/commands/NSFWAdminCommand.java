/*
 ChatParty Plugin for Minecraft Bukkit Servers
 This file: Copyright (C) 2014 Dr Daniel Naylor
    
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
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Provides access to NSFW channel administration commands.
 */
public class NSFWAdminCommand extends BaseCommandExecutor {

    public NSFWAdminCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /nsfwadmin command.
     *
     * @param cs The sender of the command.
     * @param cmnd The command.
     * @param string The alias that is used.
     * @param args The arguments to the command.
     * @return <code>true</code> if successful.
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("members")) {
            membersList(cs);
            return true;
        }

        nsfwAdminHelp(cs);
        return true;
    }

    private void nsfwAdminHelp(CommandSender cs) {
        sendMessage(cs, "--- ChatParty NSFW Admin Commands ---");
        sendMessage(cs, "/nsfwadmin members - Show all online members listening to the NSFW channel.");
    }

    /**
     * Displays a list of players listening to the NSFW channel.
     *
     * @param cs The requestor of the command.
     */
    private void membersList(CommandSender cs) {

        sendMessage(cs, "--- Online players listening to the NSFW channel ---");

        sendMessage(cs, "--------------------------------");

        List<Player> players = plugin.getNSFWChat().getNSFWChannelPlayers();

        StringBuilder onlinesb = new StringBuilder();
        if (players.isEmpty()) {
            onlinesb.append("No online players are listening to the NSFW channel.");
        } else {
            for (Player player : players) {
                if (onlinesb.length() > 0) {
                    onlinesb.append(", ");
                }

                onlinesb.append(player.getName());
            }

            onlinesb.insert(0, "Online: ").insert(0, ChatColor.GREEN);
        }

        sendMessage(cs, onlinesb.toString());
    }
}
