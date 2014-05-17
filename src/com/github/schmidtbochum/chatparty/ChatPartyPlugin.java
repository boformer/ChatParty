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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.drnaylor.chatparty.admin.AdminChat;
import uk.co.drnaylor.chatparty.commands.ACommand;
import uk.co.drnaylor.chatparty.commands.ChatCommand;
import uk.co.drnaylor.chatparty.commands.ChatPartyAdminCommand;
import uk.co.drnaylor.chatparty.commands.NSFWAdminCommand;
import uk.co.drnaylor.chatparty.commands.NSFWCommand;
import uk.co.drnaylor.chatparty.commands.NSFWListenCommand;
import uk.co.drnaylor.chatparty.commands.PCommand;
import uk.co.drnaylor.chatparty.commands.PartyAdminChatCommand;
import uk.co.drnaylor.chatparty.commands.PartyAdminCommand;
import uk.co.drnaylor.chatparty.commands.PartyCommand;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.ess.EssentialsHook;
import uk.co.drnaylor.chatparty.interfaces.IChatPartyPlugin;
import uk.co.drnaylor.chatparty.nsfw.NSFWChat;
import uk.co.drnaylor.chatparty.party.PlayerParty;

public class ChatPartyPlugin extends JavaPlugin implements IChatPartyPlugin {

    private ArrayList<OfflinePlayer> spyPlayers;
    private boolean config_invertP;
    private boolean config_toggleWithP;
    private AdminChat adminChat;
    private ChatColor config_messageColor;
    private NSFWChat nsfwChat;

    /**
     * Runs when the plugin is being enabled on the server.
     */
    @Override
    public void onEnable() {
        // copy default config
        getConfig().options().copyDefaults(true);
        saveConfig();
       
        spyPlayers = new ArrayList<OfflinePlayer>();

        for (Player player : getServer().getOnlinePlayers()) {
            registerSpy(player);
        }

        adminChat = new AdminChat(this);
        nsfwChat = new NSFWChat(this);
        
        reloadConfig();
        
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);

        // Time to register some commands!
        getCommand("p").setExecutor(new PCommand(this));
        getCommand("chat").setExecutor(new ChatCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("a").setExecutor(new ACommand(this));
        getCommand("partyadmin").setExecutor(new PartyAdminCommand(this));
        getCommand("partyadminchat").setExecutor(new PartyAdminChatCommand(this));
        getCommand("nsfw").setExecutor(new NSFWCommand(this));
        getCommand("nsfwlisten").setExecutor(new NSFWListenCommand(this));
        getCommand("nsfwadmin").setExecutor(new NSFWAdminCommand(this));
        getCommand("chatparty").setExecutor(new ChatPartyAdminCommand(this));
    }

    /**
     * Runs when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        //saveConfig();
        EssentialsHook.ClearEssentials();
    }
    
    /**
     * Saves the config file.
     * 
     * This method overrides standard Bukkit behaviour.
     */
    @Override
    public void saveConfig() {
        PlayerParty.saveConfigToFile(this);
        super.saveConfig();
    }

    /**
     * Reloads the config file, and sets up the banned word list.
     * 
     * This method overrides standard Bukkit behaviour.
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        config_invertP = getConfig().getBoolean("invertP");
        config_toggleWithP = getConfig().getBoolean("toggleWithP");
        config_messageColor = ChatColor.getByChar(getConfig().getString("messageColor").substring(1));
        if (config_messageColor == null) {
            config_messageColor = ChatColor.WHITE;
        }
        
        if (this.getNSFWChat() != null) {
            this.getNSFWChat().setupFilter(this.getConfig().getStringList("nsfwWordFilter"));
        }
        
        PlayerParty.reloadPartiesFromConfig(this);
    }
    
    /**
     * Gets the party chat template from the config file.
     *
     * @return A string representation of the template.
     */
    @Override
    public String getPartyChatTemplate() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("partyChatFormat"));
    }

    /**
     * Gets the admin chat template from the config file.
     *
     * @return A string representation of the template.
     */
    @Override
    public String getAdminChatTemplate() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("adminChatFormat"));
    }

    /**
     * Gets the NSFW chat template from the config file.
     *
     * @return A string representation of the template
     */
    @Override
    public String getNSFWChatTemplate() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("NSFWChatFormat"));
    }
    
    /**
     * Gets the ChatColor to use with messages.
     *
     * @return The ChatColor.
     *
     * @see ChatColor
     */
    @Override
    public ChatColor getMessageColour() {
        return config_messageColor;
    }

    /**
     * Gets the admin chat class.
     *
     * @return The class.
     */
    @Override
    public AdminChat getAdminChat() {
        return adminChat;
    }

    /**
     * Gets the NSFW chat class
     *
     * @return The NSFW chat class
     */
    @Override
    public NSFWChat getNSFWChat() {
        return nsfwChat;
    }

    /**
     * Gets the spies from the config list.
     */
    @Override
    public void getSpies() {
        spyPlayers.clear();
        
        List<String> UUIDlist = getConfig().getStringList("spy");
        for (String u : UUIDlist) {
            UUID uuid = UUID.fromString(u);
            OfflinePlayer player = getServer().getOfflinePlayer(uuid);
            
            if (player.hasPlayedBefore()) {
                spyPlayers.add(player);
            }
        }
        
    }
    
    /**
     * Registers a player as a spy.
     *
     * @param player The player to register as a spy.
     */
    @Override
    public void registerSpy(Player player) {
        if (player.hasPermission("chatparty.nsfw") && !getConfig().getStringList("spy").contains(player.getUniqueId().toString())) {
            List<String> st = getConfig().getStringList("spy");
            st.add(player.getUniqueId().toString());
            
            getConfig().set("spy", st);
            spyPlayers.add(player);
            
            saveConfig();
        }
    }

    /**
     * Unregisters a player as a spy.
     *
     * @param player The player to remove from the spy list.
     */
    @Override
    public void unregisterSpy(OfflinePlayer player) {
        List<String> u = getConfig().getStringList("spy");
        u.remove(player.getUniqueId().toString());
        
        getConfig().set("spy", u);
        spyPlayers.remove(player);
        
        saveConfig();
    }

    /**
     * Toggles a player's spy state.
     *
     * @param player The player to toggle.
     * @return <code>true</code> if the player now has spy, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean toggleSpy(Player player) {
        List<String> list = getConfig().getStringList("spy");
        boolean result;
        if (spyPlayers.contains(player)) {
            unregisterSpy(player);
            result = false;
        } else {
            registerSpy(player);
            result = true;
        }
        
        getConfig().set("spy", list);
        saveConfig();
        return result;
    }

    /**
     * Toggles a user's party chat status.
     *
     * @param player The player to toggle party chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    @Override
    public boolean togglePartyChat(Player player) {
        if (player.hasMetadata(MetadataState.PARTYCHAT.name())) {
            player.removeMetadata(MetadataState.PARTYCHAT.name(), this);
            return false;
        } else {
            player.setMetadata(MetadataState.PARTYCHAT.name(), new FixedMetadataValue(this, true));
            player.removeMetadata(MetadataState.ADMINCHAT.name(), this);
            player.removeMetadata(MetadataState.NSFWCHAT.name(), this);
            return true;
        }
    }

    /**
     * Toggles a user's admin chat status.
     *
     * @param player The player to toggle admin chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    @Override
    public boolean toggleAdminChat(Player player) {
        if (player.hasMetadata(MetadataState.ADMINCHAT.name())) {
            player.removeMetadata(MetadataState.ADMINCHAT.name(), this);
            return false;
        } else {
            player.setMetadata(MetadataState.ADMINCHAT.name(), new FixedMetadataValue(this, true));
            player.removeMetadata(MetadataState.PARTYCHAT.name(), this);
            player.removeMetadata(MetadataState.NSFWCHAT.name(), this);
            return true;
        }
    }

    /**
     * Toggles a user's NSFW chat status.
     *
     * @param player The player to toggle NSFW chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    @Override
    public boolean toggleNSFWChat(Player player) {
        if (player.hasMetadata(MetadataState.NSFWCHAT.name())) {
            player.removeMetadata(MetadataState.NSFWCHAT.name(), this);
            return false;
        } else {
            player.setMetadata(MetadataState.NSFWCHAT.name(), new FixedMetadataValue(this, true));
            player.removeMetadata(MetadataState.ADMINCHAT.name(), this);
            player.removeMetadata(MetadataState.PARTYCHAT.name(), this);
            return true;
        }
    }

    /**
     * Toggles whether a user is listening to the NSFW chat channel.
     *
     * @param pla The player to toggle NSFW chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    @Override
    public boolean toggleNSFWListening(Player pla) {
        if (!pla.hasMetadata(MetadataState.NSFWLISTENING.name())) {
            pla.setMetadata(MetadataState.NSFWLISTENING.name(), new FixedMetadataValue(this, true));
            return true;
        } else {
            pla.removeMetadata(MetadataState.NSFWLISTENING.name(), this);
            pla.removeMetadata(MetadataState.NSFWCHAT.name(), this);
            return false;
        }
    }

    /**
     * Toggles a user's global chat status.
     *
     * @param player The player to toggle global chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    @Override
    public boolean toggleGlobalChat(Player player) {
        if (player.hasMetadata(MetadataState.GLOBALCHATOFF.name())) {
            player.removeMetadata(MetadataState.GLOBALCHATOFF.name(), this);
            return false;
        } else {
            player.setMetadata(MetadataState.GLOBALCHATOFF.name(), new FixedMetadataValue(this, true));
            return true;
        }
    }
    
    /**
     * Handles the Party Spy message sending.
     *
     * This method will take messages to be sent to the spies and send them out
     * with the correct formatting.
     *
     * @param party The party that sent the message.
     * @param message The message to send.
     */
    @Override
    public void sendSpyPartyMessage(PlayerParty party, String message) {
        for (OfflinePlayer op : spyPlayers) {
            Player player;
            
            // If the player is not online, continue.
            if (!op.isOnline()) {
                continue;
            }
            
            player = op.getPlayer();
            if (player.hasPermission("chatparty.admin")) {
                if (!player.hasMetadata(MetadataState.INPARTY.name()) || !party.getName().equalsIgnoreCase(player.getMetadata(MetadataState.INPARTY.name()).get(0).asString())) {
                    player.sendMessage(ChatColor.GRAY + "[" + party.getShortName() + "] " + message);
                }
            } else {
                // Remove them from the list.
                unregisterSpy(op);
            }
        }
        getLogger().log(Level.INFO, "[{0}] {1}", new Object[]{party.getShortName(), message});
    }

    /**
     * Handles the Party Spy chat message sending.
     *
     * This method will take chat messages to be sent to the spies and send them
     * out with the correct formatting.
     *
     * @param party The party that sent the message.
     * @param sender The player that sent the message, or null if console.
     * @param message The message to send.
     */
    @Override
    public void sendSpyChatMessage(PlayerParty party, Player sender, String message) {
        String name = "*Console*";
        if (sender != null) {
            name = sender.getName();
        }
        
        sendSpyPartyMessage(party, name + ": " + message);
    }
    
    /**
     * Saves a player's data.
     *
     * @param player The player to save.
     */
    @Override
    public void savePlayer(Player player) {
        ConfigurationSection playerSection = getConfig().getConfigurationSection("players");

        if (player.hasMetadata(MetadataState.PARTYCHAT.name())) {
            playerSection.set(player.getUniqueId().toString(), "party");
        } else if (player.hasMetadata(MetadataState.ADMINCHAT.name())) {
            playerSection.set(player.getUniqueId().toString(), "admin");
        } else if (player.hasMetadata(MetadataState.NSFWCHAT.name())) {
            playerSection.set(player.getUniqueId().toString(), "nsfw");
        } else {
            playerSection.set(player.getUniqueId().toString(), "chat");
        }
        
        saveConfig();
    }
    
    /**
     * Removes a player from the system.
     *
     * @param playerName The player to remove.
     */
    @Override
    public void removePlayer(String playerName) {
        ConfigurationSection playerSection = getConfig().getConfigurationSection("players");
        playerSection.set(playerName, null);
        saveConfig();
    }

    /**
     * Sends a message to the selected player.
     *
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(config_messageColor + message);
    }

    /**
     * Gets whether to toggle with p.
     *
     * @return <code>true</code> if we toggle with /p, <code>false</code>
     * otherwise.
     */
    @Override
    public boolean getToggleWithP() {
        return this.config_toggleWithP;
    }

    /**
     * Gets whether to invert the chat state on /p
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    @Override
    public boolean getInvertP() {
        return this.config_invertP;
    }
}
