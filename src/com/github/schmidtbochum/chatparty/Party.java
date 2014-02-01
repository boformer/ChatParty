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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import uk.co.drnaylor.chatparty.enums.MetadataState;

public class Party {

    public final static Pattern ALPHANUMERIC = Pattern.compile("[A-Za-z0-9 ]+");

    private final ChatPartyPlugin plugin;

    private boolean disbanding = false;

    private final String name;
    private final String shortName;
    private ArrayList<String> members;
    private ArrayList<String> leaders;

    public ArrayList<Player> activePlayers;

    public Party(String name, ChatPartyPlugin plugin) {
        this.plugin = plugin;

        this.name = name;
        this.shortName = name.substring(0, 3);

        members = new ArrayList<String>();
        leaders = new ArrayList<String>();
        activePlayers = new ArrayList<Player>();
    }

    public String getName() {
        return name;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public void sendPlayerMessage(Player sender, String message) {
        String playerName = "*Console*";
        if (sender != null) {
            playerName = sender.getDisplayName();
        }
        
        String formattedMessage = plugin.getPartyChatTemplate().replace("{DISPLAYNAME}", playerName).replace("{PARTYNAME}", this.name).replace("{MESSAGE}", message);
        for (Player player : activePlayers) {
            if (player.hasPermission("chatparty.user")) {
                player.sendMessage(formattedMessage);
            }
        }
        
        plugin.sendSpyChatMessage(this, sender, message);
    }

    public void sendPartyMessage(String message) {
        String msg = String.format("%s[Party] %s", plugin.getMessageColour(), message);
        for (Player player : activePlayers) {
            if (player.hasPermission("chatparty.user")) {
                player.sendMessage(msg);
            }
        }
    }

    /**
     * Adds a player to this party.
     *
     * @param player The player to add to the party.
     */
    public void addPlayer(Player player) {
        player.removeMetadata(MetadataState.PARTYINVITE.name(), plugin);

        sendPartyMessage(player.getDisplayName() + ChatColor.GREEN + " joined the party.");
        plugin.sendSpyPartyMessage(this, player.getName() + " joined the party.");

        members.add(player.getName());
        activePlayers.add(player);

        player.setMetadata(MetadataState.INPARTY.name(), new FixedMetadataValue(plugin, name));

        plugin.sendMessage(player, "You joined the party \"" + name + "\".");
        plugin.sendMessage(player, "Chat with /p <message>");

        plugin.savePlayer(player);
        plugin.saveParty(this);
    }

    public void removePlayer(String name) {
        Player player = plugin.getServer().getPlayer(name);
        if (player != null) {
            removePlayer(player, false);
        } else {
            members.remove(name);
            leaders.remove(name);
        }
    }

    /**
     * Removes an online player from the party.
     *
     * @param player The player to remove.
     * @param kicked Set to <code>true</code> if the player was kicked
     */
    public void removePlayer(Player player, boolean kicked) {
        player.removeMetadata(MetadataState.INPARTY.name(), plugin);
        player.removeMetadata(MetadataState.PARTYLEADER.name(), plugin);

        if (!disbanding) {
            leaders.remove(player.getName());
            members.remove(player.getName());
            activePlayers.remove(player);
        }

        if (kicked) {
            sendPartyMessage(player.getDisplayName() + ChatColor.GREEN + " was kicked from the party.");
            plugin.sendSpyPartyMessage(this, player.getName() + " was kicked from the party.");
            plugin.sendMessage(player, "You were kicked from the party \"" + name + "\".");
        } else {
            sendPartyMessage(player.getDisplayName() + ChatColor.GREEN + " left the party.");
            plugin.sendSpyPartyMessage(this, player.getName() + " left the party.");
            plugin.sendMessage(player, "You left the party \"" + name + "\".");
        }

        plugin.savePlayer(player);
        
        if (leaders.isEmpty()) {
            sendPartyMessage("The party was disbanded because all leaders left.");
            plugin.sendSpyPartyMessage(this, "The party was disbanded.");
            disband();
        }

        plugin.saveParty(this);
    }

    /**
     * Kicks a player, based on the leaderPlayer.
     *
     * @param leaderPlayer The player that is doing the kicking.
     * @param player The player that is being kicked.
     */
    public void kickPlayer(Player leaderPlayer, OfflinePlayer player) {
        if (!leaderPlayer.hasMetadata(MetadataState.PARTYLEADER.name())) {
            plugin.sendMessage(leaderPlayer, "Only party leaders can kick other players.");
            return;
        }

        if (leaders.contains(player.getName())) {
            plugin.sendMessage(leaderPlayer, "You can't kick party leaders.");
            return;
        }

        if (!members.contains(player.getName())) {
            plugin.sendMessage(leaderPlayer, "The player is not a member of your party.");
            return;
        }

        if (player.isOnline()) {
            removePlayer(player.getPlayer(), true);
        } else {
            removePlayer(player.getName());
        }
    }

    /**
     * Disbands the party.
     */
    public void disband() {
        if (disbanding) {
            return;
        }

        disbanding = true;

        Iterator<String> it = members.iterator();
        while (it.hasNext()) {
            removePlayer(it.next());
        }
        
        Iterator<String> it2 = leaders.iterator();
        while (it2.hasNext()) {
            removePlayer(it2.next());
        }
        
        plugin.getActiveParties().remove(name);

        leaders = null;
        members = null;

        plugin.saveConfig();

        plugin.getLogger().log(Level.INFO, "Disbanded the chat party \"{0}\".", name);
    }

    public Map<MemberType, List<String>> getMembers() {
        Map<MemberType, List<String>> ret = new HashMap<MemberType, List<String>>();

        ret.put(MemberType.MEMBER, members);
        ret.put(MemberType.LEADER, leaders);
        return ret;
    }

    /**
     * Sets a player as the leader of the party.
     *
     * @param promotedPlayer The player to set as a party leader.
     * @return <code>true</code> if the player was promoted, <code>false</code>
     * if the user was not part of the party in the first place.
     */
    public boolean addLeader(OfflinePlayer promotedPlayer) {
        if (!members.remove(promotedPlayer.getName())) {
            return false;
        }

        leaders.add(promotedPlayer.getName());

        Player onlinePlayer = promotedPlayer.getPlayer();

        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            onlinePlayer.setMetadata(MetadataState.PARTYLEADER.name(), new FixedMetadataValue(plugin, true));
        }
        plugin.saveParty(this);

        sendPartyMessage(promotedPlayer.getName() + ChatColor.GREEN + " is now a leader of the party.");
        plugin.sendSpyPartyMessage(this, promotedPlayer.getName() + " is now a leader of the party.");
        return true;
    }

    /**
     * Creates a party.
     *
     * @param player The player that wishes to create the party.
     * @param partyName The name of the party.
     * @param plugin This plugin.
     * @return The party.
     */
    public static Party create(Player player, String partyName, ChatPartyPlugin plugin) {

        if (partyName.length() > 15) {
            plugin.sendMessage(player, "This name is too long! (3-15 letters)");
            return null;
        }
        if (partyName.length() < 3) {
            plugin.sendMessage(player, "This name is too short! (3-15 letters)");
            return null;
        }

        if (!Party.validateName(partyName)) {
            plugin.sendMessage(player, "\"" + partyName + "\" is not a valid name. Allowed characters are A-Z, a-z, 0-9.");
            return null;
        }

        Party party = new Party(partyName, plugin);

        party.leaders.add(player.getName());
        party.activePlayers.add(player);

        player.setMetadata(MetadataState.INPARTY.name(), new FixedMetadataValue(plugin, party.name));
        player.setMetadata(MetadataState.PARTYLEADER.name(), new FixedMetadataValue(plugin, true));

        plugin.getActiveParties().put(partyName, party);

        plugin.savePlayer(player);
        plugin.saveParty(party);

        plugin.sendMessage(player, "You created the party \"" + party.name + "\".");
        plugin.sendMessage(player, "Invite your friends with /party invite <player>");
        plugin.sendMessage(player, "Send a message to your party with /p <message>");

        plugin.getLogger().log(Level.INFO, "Created the chat party \"{0}\".", party.name);
        return party;
    }

    public static Party loadParty(String partyName, ChatPartyPlugin pluginInstance) {
        Party party = new Party(partyName, pluginInstance);

        ConfigurationSection partySection = pluginInstance.getConfig().getConfigurationSection("parties." + partyName);

        if (partySection == null || partySection.getStringList("leaders").isEmpty()) {
            return null;
        }

        party.leaders = (ArrayList<String>) partySection.getStringList("leaders");
        party.members = (ArrayList<String>) partySection.getStringList("members");

        for (Player player : pluginInstance.getServer().getOnlinePlayers()) {
            if (party.leaders.contains(player.getName()) || party.members.contains(player.getName())) {
                party.activePlayers.add(player);
            }
        }

        pluginInstance.getActiveParties().put(party.getName(), party);
        return party;
    }

    /**
     * Validates a party name.
     *
     * @param name The name to validate
     * @return <code>true</code> if successful.
     */
    private static boolean validateName(String name) {
        Matcher m = ALPHANUMERIC.matcher(name);
        return m.matches();
    }

    public enum MemberType {

        LEADER,
        MEMBER
    }
}
