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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import supercheese200.NSFWChat.NSFWChat;
import uk.co.drnaylor.chatparty.admin.AdminChat;
import uk.co.drnaylor.chatparty.commands.ACommand;
import uk.co.drnaylor.chatparty.commands.ChatCommand;
import uk.co.drnaylor.chatparty.commands.ChatPartyAdminCommand;
import uk.co.drnaylor.chatparty.commands.NSFWCommand;
import uk.co.drnaylor.chatparty.commands.NSFWListenCommand;
import uk.co.drnaylor.chatparty.commands.PCommand;
import uk.co.drnaylor.chatparty.commands.PartyAdminChatCommand;
import uk.co.drnaylor.chatparty.commands.PartyAdminCommand;
import uk.co.drnaylor.chatparty.commands.PartyCommand;
import uk.co.drnaylor.chatparty.interfaces.IChatPartyPlugin;

public class ChatPartyPlugin extends JavaPlugin implements IChatPartyPlugin {

    private HashMap<String, Party> activeParties;
    private ArrayList<Player> spyPlayers;
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
        getCommand("chatparty").setExecutor(new ChatPartyAdminCommand(this));
    }

    /**
     * Runs when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        //saveConfig();
    }

    /**
     * Reloads the config file, and sets up the banned word list.
     * 
     * This method overrides standard Bukkit behaviour.
     */
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (this.getNSFWChat() != null) {
            this.getNSFWChat().setupFilter(this.getConfig().getStringList("nsfwWordFilter"));
        }
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
     * Saves a party.
     *
     * @param party The party to save the data for.
     */
    @Override
    public void saveParty(Party party) {
        ConfigurationSection partySection = getConfig().getConfigurationSection("parties").createSection(party.getName());
        partySection.set("leaders", party.getMembers().get(MemberType.LEADER));
        partySection.set("members", party.getMembers().get(MemberType.MEMBER));
        saveConfig();
        reloadConfig();
    }

    /**
     * Gets the party a player belongs to.
     *
     * @param player The player.
     * @return The party that player is part of, or null if not in a party.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean togglePartyChat(Player player) {
        if (player.hasMetadata("partyToggle")) {
            player.removeMetadata("partyToggle", this);
            return false;
        } else {
            player.setMetadata("partyToggle", new FixedMetadataValue(this, true));
            player.removeMetadata("adminToggle", this);
            player.removeMetadata("nsfwToggle", this);
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
        if (player.hasMetadata("adminToggle")) {
            player.removeMetadata("adminToggle", this);
            return false;
        } else {
            player.setMetadata("adminToggle", new FixedMetadataValue(this, true));
            player.removeMetadata("partyToggle", this);
            player.removeMetadata("nsfwToggle", this);
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
        if (player.hasMetadata("nsfwToggle")) {
            player.removeMetadata("nsfwToggle", this);
            return false;
        } else {
            player.setMetadata("nsfwToggle", new FixedMetadataValue(this, true));
            player.removeMetadata("adminToggle", this);
            player.removeMetadata("partyToggle", this);
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
        if (!pla.hasMetadata("nsfwlistening")) {
            pla.setMetadata("nsfwlistening", new FixedMetadataValue(this, true));
            return true;
        } else {
            pla.removeMetadata("nsfwlistening", this);
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
     * This method will take messages to be sent to the spies and send them out
     * with the correct formatting.
     *
     * @param party The party that sent the message.
     * @param message The message to send.
     */
    @Override
    public void sendSpyPartyMessage(Party party, String message) {
        for (Player player : spyPlayers) {
            if (player.hasPermission("chatparty.admin") && (!player.hasMetadata("party") || !party.getName().equalsIgnoreCase(player.getMetadata("party").get(0).asString()))) {
                player.sendMessage(ChatColor.GRAY + "[" + party.getShortName() + "] " + message);
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
     * @param sender The player that sent the message.
     * @param message The message to send.
     */
    @Override
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
    @Override
    public Party loadParty(String name) {
        Party party = activeParties.get(name);

        if (party == null) {
            party = Party.loadParty(name, this);
        }

        return party;
    }

    /**
     * Removes a party from the list.
     *
     * @param party The party to remove.
     */
    void removeActiveParty(Party party) {
        activeParties.remove(party.getName());
    }

    /**
     * Saves a player's data.
     *
     * @param player The player to save.
     */
    @Override
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

    /**
     * Gets the active parties.
     *
     * @return The active parties.
     */
    @Override
    public Map<String, Party> getActiveParties() {
        return activeParties;
    }
}
