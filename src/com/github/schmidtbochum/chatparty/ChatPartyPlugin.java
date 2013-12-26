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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.drnaylor.chatparty.admin.AdminChat;
import uk.co.drnaylor.chatparty.commands.ACommand;
import uk.co.drnaylor.chatparty.commands.ChatCommand;
import uk.co.drnaylor.chatparty.commands.PCommand;
import uk.co.drnaylor.chatparty.commands.PartyCommand;

public class ChatPartyPlugin extends JavaPlugin {

    private HashMap<String, Party> activeParties;
    private ArrayList<Player> spyPlayers;
    private boolean config_invertP;
    private boolean config_toggleWithP;
    private AdminChat adminChat;

    public ChatColor config_messageColor;
    public final boolean GUILD_MODE = false;
    public final String TEXT_PARTY = (GUILD_MODE ? "guild" : "party");
    public final String TEXT_PARTY2 = (GUILD_MODE ? "Guild" : "Party");
    public final String TEXT_PARTIES = (GUILD_MODE ? "guilds" : "parties");
    public final String TEXT_P = (GUILD_MODE ? "g" : "p");

    /**
     * Runs when the plugin is being enabled on the server.
     */
    @Override
    public void onEnable() {
        // copy default config
        getConfig().options().copyDefaults(true);
        saveConfig();

        config_invertP = getConfig().getBoolean("invertP");
        config_toggleWithP = getConfig().getBoolean("toggleWithP");
        config_messageColor = ChatColor.getByChar(getConfig().getString("messageColor").substring(1));
        if (config_messageColor == null) {
            config_messageColor = ChatColor.WHITE;
        }

        activeParties = new HashMap<String, Party>();
        spyPlayers = new ArrayList<Player>();

        for (Player player : getServer().getOnlinePlayers()) {
            registerSpy(player);
        }
        
        adminChat = new AdminChat(this);

        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);

        // Time to register some commands!
        getCommand("p").setExecutor(new PCommand(this));
        getCommand("chat").setExecutor(new ChatCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("a").setExecutor(new ACommand(this));
    }

    /**
     * Runs when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        //saveConfig();
    }

    /**
     * Gets the party chat template from the config file.
     * 
     * @return A string representation of the template.
     */
    public String getPartyChatTemplate() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("partyChatFormat"));
    }
    
    /**
     * Gets the admin chat template from the config file.
     * 
     * @return A string representation of the template.
     */
    public String getAdminChatTemplate() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("adminChatFormat"));
    }
    
    /**
     * Gets the admin chat class.
     * 
     * @return The class.
     */
    public AdminChat getAdminChat() {
        return adminChat;
    }
    
    /**
     * Saves a party.
     *
     * @param party The party to save the data for.
     */
    public void saveParty(Party party) {
        ConfigurationSection partySection = getConfig().getConfigurationSection("parties").createSection(party.name);
        partySection.set("leaders", party.leaders);
        partySection.set("members", party.members);
        saveConfig();
        reloadConfig();
    }

    /**
     * Gets the party a player belongs to.
     *
     * @param player The player.
     * @return The party that player is part of, or null if not in a party.
     */
    public Party getPlayerParty(Player player) {
        String partyName = getConfig().getConfigurationSection("players").getString(player.getName());
        if (partyName != null) {
            return loadParty(partyName);
        } else {
            return null;
        }
    }

    /**
     * Registers a player as a spy.
     *
     * @param player The player to register as a spy.
     */
    public void registerSpy(Player player) {
        if (getConfig().getStringList("spy").contains(player.getName())) {
            spyPlayers.add(player);
        }
    }

    /**
     * Unregisters a player as a spy.
     *
     * @param player The player to remove from the spy list.
     */
    public void unregisterSpy(Player player) {
        spyPlayers.remove(player);
    }

    /**
     * Toggles a player's spy state.
     *
     * @param player The player to toggle.
     * @return <code>true</code> if the player now has spy, <code>false</code>
     * otherwise.
     */
    public boolean toggleSpy(Player player) {
        List<String> list = getConfig().getStringList("spy");
        boolean result;
        if (spyPlayers.contains(player)) {
            spyPlayers.remove(player);
            list.remove(player.getName());
            result = false;
        } else {
            spyPlayers.add(player);
            list.add(player.getName());
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
    public boolean togglePartyChat(Player player) {
        if (player.hasMetadata("partyToggle")) {
            player.removeMetadata("partyToggle", this);
            return false;
        } else {
            player.setMetadata("partyToggle", new FixedMetadataValue(this, true));
            player.removeMetadata("adminToggle", this);
            return true;
        }
    }

    /**
     * Toggles a user's admin chat status.
     * 
     * @param player The player to toggle admin chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    public boolean toggleAdminChat(Player player) {
        if (player.hasMetadata("adminToggle")) {
            player.removeMetadata("adminToggle", this);
            return false;
        } else {
            player.setMetadata("adminToggle", new FixedMetadataValue(this, true));
            player.removeMetadata("partyToggle", this);
            return true;
        }
    }

    /**
     * Toggles a user's global chat status.
     * 
     * @param player The player to toggle global chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    public boolean toggleGlobalChat(Player player) {
        if (player.hasMetadata("globalChatToggle")) {
            player.removeMetadata("globalChatToggle", this);
            return false;
        } else {
            player.setMetadata("globalChatToggle", new FixedMetadataValue(this, true));
            return true;
        }
    }

    /**
     * Handles the Party Spy message sending.
     * 
     * This method will take messages to be sent to the spies and send them out with the correct formatting.
     * 
     * @param party The party that sent the message.
     * @param message The message to send.
     */
    public void sendSpyPartyMessage(Party party, String message) {
        for (Player player : spyPlayers) {
            if (player.hasPermission("chatparty.admin") && (!player.hasMetadata("party") || !party.name.equalsIgnoreCase(player.getMetadata("party").get(0).asString()))) {
                player.sendMessage(ChatColor.GRAY + "[" + party.shortName + "] " + message);
            }
        }
        getLogger().info("[" + party.shortName + "] " + message);
    }

    /**
     * Handles the Party Spy chat message sending.
     * 
     * This method will take chat messages to be sent to the spies and send them out with the correct formatting.
     * 
     * @param party The party that sent the message.
     * @param sender The player that sent the message.
     * @param message The message to send.
     */
    public void sendSpyChatMessage(Party party, Player sender, String message) {
        sendSpyPartyMessage(party, sender.getName() + ": " + message);
    }

    /**
     * Gets a party based on it's name.
     *
     * @param name The name of the party.
     * @return The party object.
     *
     * @see Party
     */
    public Party loadParty(String name) {
        Party party = activeParties.get(name);

        if (party == null) {
            party = new Party(name, this);

            ConfigurationSection partySection = getConfig().getConfigurationSection("parties." + name);

            if (partySection == null || partySection.getStringList("leaders").size() == 0) {
                return null;
            }

            party.leaders = (ArrayList<String>) partySection.getStringList("leaders");
            party.members = (ArrayList<String>) partySection.getStringList("members");

            for (Player player : getServer().getOnlinePlayers()) {
                if (party.leaders.contains(player.getName()) || party.members.contains(player.getName())) {
                    party.activePlayers.add(player);
                }
            }
        }

        return party;
    }

    /**
     * Removes a party from the list.
     * 
     * @param party The party to remove.
     */
    void removeActiveParty(Party party) {
        activeParties.remove(party.name);
    }

    /**
     * Saves a player's data.
     *
     * @param player The player to save.
     */
    public void savePlayer(Player player) {
        ConfigurationSection playerSection = getConfig().getConfigurationSection("players");

        if (!player.hasMetadata("party")) {
            playerSection.set(player.getName(), null);
        } else {
            String partyName = player.getMetadata("party").get(0).asString();
            playerSection.set(player.getName(), partyName);
        }
        saveConfig();
    }

    /**
     * Removes a player from the system.
     *
     * @param playerName The player to remove.
     */
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
    public void sendMessage(Player player, String message) {
        player.sendMessage(config_messageColor + message);
    }

    /**
     * Gets whether to toggle with p.
     *
     * @return <code>true</code> if we toggle with /p, <code>false</code>
     * otherwise.
     */
    public boolean getToggleWithP() {
        return this.config_toggleWithP;
    }

    /**
     * Gets whether to invert the chat state on /p
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    public boolean getInvertP() {
        return this.config_invertP;
    }

    /**
     * Gets the active parties.
     *
     * @return The active parties.
     */
    public Map<String, Party> getActiveParties() {
        return activeParties;
    }
}
