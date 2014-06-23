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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.enums.PlayerPartyRank;
import uk.co.drnaylor.chatparty.ess.EssentialsHook;
import uk.co.drnaylor.chatparty.party.PlayerParty;

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

        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (n.contains(event.getPlayer().getUniqueId().toString())) {
            player.setMetadata(MetadataState.NSFWLISTENING.name(), new FixedMetadataValue(plugin, true));
            plugin.sendMessage(player, "You are listening to the NSFW channel.");
        }
        
        PlayerParty party = PlayerParty.getPlayerParty(player);
        if (party != null) {
            player.setMetadata(MetadataState.INPARTY.name(), new FixedMetadataValue(plugin, party.getName()));
            
            // Set whether they are a leader in the metadata.
            if (party.getPlayerRank(player) == PlayerPartyRank.LEADER) {
                player.setMetadata(MetadataState.PARTYLEADER.name(), new FixedMetadataValue(plugin, true));
            } else {
                player.removeMetadata(MetadataState.PARTYLEADER.name(), plugin);
            }
        }
        
        // Config strings.
        String chatChannel = plugin.getConfig().getString("lastPlayerChannel." + player.getUniqueId().toString());
        if (chatChannel == null) {
            return;
        }
        
        // Restore chat channels.
        if (chatChannel.equalsIgnoreCase("party") && player.hasMetadata(MetadataState.INPARTY.name())) {
            player.setMetadata(MetadataState.PARTYCHAT.name(), new FixedMetadataValue(plugin, true));
        } else if (chatChannel.equalsIgnoreCase("admin") && player.hasPermission("chatparty.admin") ) {
            player.setMetadata(MetadataState.ADMINCHAT.name(), new FixedMetadataValue(plugin, true));
        } else if (chatChannel.equalsIgnoreCase("nsfw") && player.hasPermission("chatparty.nsfw") ) {
            player.setMetadata(MetadataState.NSFWCHAT.name(), new FixedMetadataValue(plugin, true));
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
        
        // Save chat channel.
        if (player.hasMetadata(MetadataState.PARTYCHAT.name())) {
            plugin.getConfig().set("lastPlayerChannel." + player.getUniqueId().toString(), "party");
        } else if (player.hasMetadata(MetadataState.ADMINCHAT.name())) {
            plugin.getConfig().set("lastPlayerChannel." + player.getUniqueId().toString(), "admin");
        } else if (player.hasMetadata(MetadataState.NSFWCHAT.name())) {
            plugin.getConfig().set("lastPlayerChannel." + player.getUniqueId().toString(), "nsfw");
        }
        
        if (player.hasMetadata(MetadataState.INPARTY.name())) {
            player.removeMetadata(MetadataState.INPARTY.name(), plugin);
            player.removeMetadata(MetadataState.PARTYLEADER.name(), plugin);
        }

        List<String> n = plugin.getConfig().getStringList("nsfwListeners");
        if (player.hasMetadata(MetadataState.NSFWLISTENING.name())) {
            if (!n.contains(event.getPlayer().getUniqueId().toString())) {
                n.add(event.getPlayer().getUniqueId().toString());
            }
            
            player.removeMetadata(MetadataState.NSFWLISTENING.name(), plugin);
        } else {
            if (n.contains(event.getPlayer().getUniqueId().toString())) {
                n.remove(event.getPlayer().getUniqueId().toString());
            }
        }

        plugin.getConfig().set("nsfwListeners", n);
        plugin.saveConfig();
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
            if (EssentialsHook.isMuted(player)) {
                plugin.sendMessage(player, "You cannot speak if you are muted!");
                event.setCancelled(true);
                return;
            }
                        
            player.removeMetadata(MetadataState.IGNORE.name(), plugin);
        } else if (player.hasMetadata(MetadataState.ADMINCHAT.name())) {
            plugin.getAdminChat().sendAdminMessage(player, event.getMessage());
            event.setCancelled(true);
            return;
        } else if (player.hasMetadata(MetadataState.NSFWCHAT.name())) {    
            if (EssentialsHook.isMuted(player)) {
                plugin.sendMessage(player, "You cannot speak if you are muted!");
                event.setCancelled(true);
                return;
            }
            
            plugin.getNSFWChat().sendNSFWMessage(player, event.getMessage());
            event.setCancelled(true);
            return;
        } else if (player.hasMetadata(MetadataState.PARTYCHAT.name()) && player.hasMetadata(MetadataState.INPARTY.name())) {
            
            // The party
            PlayerParty party = PlayerParty.getPlayerParty(player);
            if (party == null) {
                // The party doesn't exist any more, so let's remove the metadata.
                player.removeMetadata(MetadataState.PARTYCHAT.name(), plugin);
                player.removeMetadata(MetadataState.INPARTY.name(), plugin);
                return;
            }
            
            // If muted, honour that.
            if (EssentialsHook.isMuted(player)) {
                plugin.sendMessage(player, "You cannot speak if you are muted!");
                event.setCancelled(true);
                return;
            }
            
            String message = event.getMessage();
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
    
    /**
     * Fires when a block is placed.
     * 
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onPlayerBlockPlace(BlockPlaceEvent event) {
        
        if (!(event.getBlock().getState() instanceof Sign)) {
            return;
        }
        
        Sign s = (Sign)event.getBlock().getState();
        checkLines(s.getLines(), event, event.getPlayer());
    }
    
    /**
     * Fires when a sign is changed.
     * 
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onPlayerSignChange(SignChangeEvent event) {
        checkLines(event.getLines(), event, event.getPlayer());
    }
    
    /**
     * Checks the lines on the signs for censored words, and cancels the event, if required.
     * 
     * @param lines The lines to check.
     * @param event The cancellable event.
     * @param player The player performing the action.
     */
    private void checkLines(String[] lines, Cancellable event, Player player) {
      
        // Check each line for a banned word.
        for (String s : lines) {
            if (plugin.getNSFWChat().containsBannedWord(s)) {
                // Cancel event, notify player.
                event.setCancelled(true);
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "You cannot use NSFW words on a sign.");
                }
                
                return;
            }
        }
    }
}
