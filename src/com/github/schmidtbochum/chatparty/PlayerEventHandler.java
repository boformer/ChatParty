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
import uk.co.drnaylor.chatparty.enums.MetadataState;

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

        // NSFW registration
        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (n.contains(event.getPlayer().getName().toLowerCase())) {
            player.setMetadata(MetadataState.NSFWLISTENING.name(), new FixedMetadataValue(plugin, true));
            plugin.sendMessage(player, "You are listening to the NSFW channel.");
        }
        
        // Party registration
        Party party = plugin.getPlayerParty(player);

        if (party == null) {
            player.removeMetadata(MetadataState.INPARTY.name(), plugin);
            return;
        }

        player.setMetadata(MetadataState.INPARTY.name(), new FixedMetadataValue(plugin, party.getName()));

        if (party.getMembers().get(MemberType.LEADER).contains(player.getName())) {
            player.setMetadata(MetadataState.PARTYLEADER.name(), new FixedMetadataValue(plugin, true));
        } else {
            player.removeMetadata(MetadataState.PARTYLEADER.name(), plugin);
        }

        party.activePlayers.add(player);
    }

    /**
     * Fires an event when the player leaves the server.
     *
     * @param event The event to handle.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(MetadataState.INPARTY.name())) {
            String partyName = player.getMetadata(MetadataState.INPARTY.name()).get(0).asString();
            Party party = plugin.loadParty(partyName);

            party.activePlayers.remove(player);

            player.removeMetadata(MetadataState.INPARTY.name(), plugin);
            player.removeMetadata(MetadataState.PARTYLEADER.name(), plugin);
        }

        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (player.hasMetadata(MetadataState.NSFWLISTENING.name())) {
            if (!n.contains(event.getPlayer().getName().toLowerCase())) {
                n.add(event.getPlayer().getName().toLowerCase());
            }
            
            player.removeMetadata(MetadataState.NSFWLISTENING.name(), plugin);
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

        boolean hasIgnore = player.hasMetadata(MetadataState.IGNORE.name());

        if (hasIgnore) {
            player.removeMetadata(MetadataState.IGNORE.name(), plugin);
        } else if (player.hasMetadata(MetadataState.ADMINCHAT.name())) {
            plugin.getAdminChat().sendAdminMessage(player, event.getMessage());
            event.setCancelled(true);
            return;
        } else if (player.hasMetadata(MetadataState.NSFWCHAT.name())) {
            plugin.getNSFWChat().sendNSFWMessage(player, event.getMessage());
            event.setCancelled(true);
            return;
        } else if (player.hasMetadata(MetadataState.PARTYCHAT.name()) && player.hasMetadata(MetadataState.INPARTY.name())) {
            String message = event.getMessage();

            String partyName = player.getMetadata(MetadataState.INPARTY.name()).get(0).asString();
            Party party = plugin.loadParty(partyName);

            party.sendPlayerMessage(player, message);

            event.setCancelled(true);
            return;
        } else if (player.hasMetadata(MetadataState.GLOBALCHATOFF.name())) {
            plugin.sendMessage(player, "Message cancelled. Type /chat to enable the global chat.");

            event.setCancelled(true);
            return;
        }
        
        // If we are here, then check for banned words if the feature is enabled.
        if (plugin.getNSFWChat().containsBannedWord(event.getMessage()) && plugin.getConfig().getBoolean("censorGlobalChat")) {
            plugin.sendMessage(player, String.format("%sSwearing is not allowed in the global chat!", ChatColor.RED));
            event.setCancelled(true);
            return;
        }

        if (hasIgnore) {
            return;
        }

        Set<Player> recipients = event.getRecipients();

        /* Set iterator */
        Iterator<Player> recipientIterator = recipients.iterator();

        while (recipientIterator.hasNext()) {
            if (recipientIterator.next().hasMetadata(MetadataState.GLOBALCHATOFF.name())) {
                // Remove an object from a set with the iterator
                recipientIterator.remove();
            }
        }

    }
}
