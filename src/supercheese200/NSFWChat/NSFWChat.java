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

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.co.drnaylor.chatparty.interfaces.IChatPartyPlugin;

/**
 * Handles the NSFW channel. 
 */
public class NSFWChat {

    private final IChatPartyPlugin plugin;

    private final HashSet<String> bannedWords = new HashSet<String>();
    
    private final Pattern wordFilter = Pattern.compile("[a-zA-Z0-9\\,\\._\\-\\?\\!\\*]+", Pattern.CASE_INSENSITIVE);
    
    public NSFWChat(IChatPartyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates the set that contains the list of banned words.
     * 
     * @param strings The strings that contain the banned words.
     */
    public void setupFilter(Collection<String> strings) {
        bannedWords.clear();
        for (String s : strings) {
            bannedWords.add(s.toLowerCase());
        }
    }
    
    /**
     * Checks to see whether a banned word exists in the dictionary.
     * 
     * @param chat The chat message to scan.
     * @return <code>true</code> if a banned word is found, <code>false</code> otherwise.
     */
    public boolean containsBannedWord(String chat) {
        ChatColor.stripColor(chat);
        Scanner scanner = new Scanner(chat.toLowerCase());
        
        while(scanner.hasNext(wordFilter)) {
            String s = scanner.next(wordFilter);
            s = s.replaceAll("[\\,\\._\\-\\?\\!\\*]", "");
            if (bannedWords.contains(s)) {
                // Banned word.
                return true;
            } else if (s.endsWith("s") && bannedWords.contains(s.substring(0, s.length() - 1))) {
                // Pluralised banned word.
                return true;
            }
        }
        
        return false;
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
        plugin.getServer().getConsoleSender().sendMessage(formattedMessage);
    }
}
