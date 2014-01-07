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
package com.github.schmidtbochum.chatparty;

import com.github.schmidtbochum.chatparty.Party.MemberType;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerEventHandler implements Listener {

    // Private Fields
    private final ChatPartyPlugin plugin;

    /**
     * Constructs the event handler.
     * 
     * @param plugin The plugin object to be used with the events.
     */
    public PlayerEventHandler(ChatPartyPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Fires when a player joins the server.
     * 
     * @param event 
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.registerSpy(player);

        Party party = plugin.getPlayerParty(player);

        if (party == null) {
            player.removeMetadata("party", plugin);
            return;
        }

        player.setMetadata("party", new FixedMetadataValue(plugin, party.getName()));

        if (party.getMembers().get(MemberType.LEADER).contains(player.getName())) {
            player.setMetadata("isPartyLeader", new FixedMetadataValue(plugin, true));
        } else {
            player.removeMetadata("isPartyLeader", plugin);
        }
       
        party.activePlayers.add(player);

        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (n.contains(event.getPlayer().getName().toLowerCase())) {
            player.setMetadata("nsfwlistening",  new FixedMetadataValue(plugin, true));
        }

    }

    /**
     * Fires an event when the player leaves the server.
     * 
     * @param event The event to handle.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("party")) {
            String partyName = player.getMetadata("party").get(0).asString();
            Party party = plugin.loadParty(partyName);

            party.activePlayers.remove(player);

            player.removeMetadata("party", plugin);
            player.removeMetadata("isPartyLeader", plugin);
        }
        
        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (player.hasMetadata("nsfwlistening")) {
            if (!n.contains(event.getPlayer().getName().toLowerCase())) {
                n.add(event.getPlayer().getName().toLowerCase());
            }
        } else {
            if (n.contains(event.getPlayer().getName().toLowerCase())) {
                n.remove(event.getPlayer().getName().toLowerCase());
            }
        }
        
        plugin.getConfig().set("nsfwListeners", n);
        plugin.saveConfig();
        
        plugin.unregisterSpy(player);
    }

    /**
     * Fires an event when the player chats on the server.
     * 
     * @param event 
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("ignore")) {
            player.removeMetadata("ignore", plugin);
            return;
        }
        
        if (player.hasMetadata("adminToggle")) {
            plugin.getAdminChat().sendAdminMessage(player, event.getMessage());
            event.setCancelled(true);
        }
        else if (player.hasMetadata("nsfwToggle")) {
            plugin.getNSFWChat().sendNSFWMessage(player, event.getMessage());
            event.setCancelled(true);
        }
        else if (player.hasMetadata("partyToggle") && player.hasMetadata("party")) {
            String message = event.getMessage();

            String partyName = player.getMetadata("party").get(0).asString();
            Party party = plugin.loadParty(partyName);

            party.sendPlayerMessage(player, message);

            event.setCancelled(true);
        } else if (player.hasMetadata("globalChatToggle")) {
            plugin.sendMessage(player, "Message cancelled. Type /chat to enable the global chat.");

            event.setCancelled(true);
        } else {
            // If we are here, then check for banned words if the feature is enabled.
            if (plugin.getNSFWChat().containsBannedWord(event.getMessage()) && plugin.getConfig().getBoolean("censorGlobalChat")) {
                plugin.sendMessage(player, String.format("%sSwearing is not allowed in the global chat!", ChatColor.RED));
                event.setCancelled(true);
                return;
            }
            
            Set<Player> recipients = event.getRecipients();

            /* Set iterator */
            Iterator<Player> recipientIterator = recipients.iterator();

            while (recipientIterator.hasNext()) {
                if (recipientIterator.next().hasMetadata("globalChatToggle")) {
                    // Remove an object from a set with the iterator
                    recipientIterator.remove();
                }
            }
        }
    }
}
