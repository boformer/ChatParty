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
package uk.co.drnaylor.chatparty.party;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.enums.PlayerPartyRank;
import uk.co.drnaylor.chatparty.enums.PlayerRemoveReason;
import uk.co.drnaylor.chatparty.exceptions.ChatPartyException;

/**
 * Class to represent a player party.
 */
public final class PlayerParty {
    
    private final static Set<PlayerParty> parties = new HashSet<PlayerParty>();
    private final static Pattern partyNameRegex = Pattern.compile("^[A-Za-z0-9]{4,}$");
    
    private final String partyName;
    private final Map<OfflinePlayer, PlayerPartyRank> members = new HashMap<OfflinePlayer, PlayerPartyRank>();
    private final ChatPartyPlugin plugin;
    private final String tag;
    
    // Public Statics
    
    /**
     * Gets all the parties in the system.
     * @return The @link{Set} of @link{PlayerParty} objects.
     */
    public static Set<PlayerParty> getParties() {
        return parties;
    }
    
    /**
     * Gets the party of the selected player.
     * 
     * @param player The @link{OfflinePlayer} to get the @link{PlayerParty} of.
     * @return The @link{PlayerParty}, if the player is a member of one, or <code>null</code>.
     */
    public static PlayerParty getPlayerParty(OfflinePlayer player) {
        for (PlayerParty party : parties) {
            if (party.members.containsKey(player)) {
                return party;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a party from it's name. Case insensitive.
     * 
     * @param name The name of the party.
     * @return The @link{PlayerParty} if it exists, otherwise <code>null</code>
     */
    public static PlayerParty getPartyFromName(String name) {
        for (PlayerParty party : parties) {
            if (party.partyName.equalsIgnoreCase(name)) {
                return party;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a party from it's tag. Case insensitive.
     * 
     * @param tag The tag of the party.
     * @return The @link{PlayerParty} if it exists, otherwise <code>null</code>
     */
    public static PlayerParty getPartyFromTag(String tag) {
        for (PlayerParty party : parties) {
            if (party.tag.equalsIgnoreCase(tag)) {
                return party;
            }
        }
        
        return null;
    }
    
    /**
     * Creates a player party.
     * 
     * @param player The player who created the party.
     * @param name The name of the proposed party.
     * @param plugin The plugin. Must not be <code>null</code>.
     * @return The party.
     * @throws ChatPartyException If the party could not be created.
     */
    public static PlayerParty createPlayerParty(OfflinePlayer player, String name, ChatPartyPlugin plugin) throws ChatPartyException {
        if (plugin == null) {
            throw new IllegalArgumentException("The ChatPartyPlugin object was not supplied.");
        }
        
        // Check the party name is alphanumeric only.
        if (!partyNameRegex.matcher(name).matches()) {
            throw new ChatPartyException(String.format("The party name '%s' is not valid.", name));
        }
        
        // Check for party validity.
        if (getPartyFromName(name) != null) {
            throw new ChatPartyException(String.format("The party '%s' already exists.", name));
        }
        
        // If the player is in another party, kick them from it.
        PlayerParty currentParty = getPlayerParty(player);
        if (currentParty != null) {
            currentParty.removePlayer(player, null, PlayerRemoveReason.MOVED_PARTY);
        }
        
        // Now create the party. First, get a tag.
        String tag = name.substring(0, 3);
        int count = 0;
        while (getPartyFromTag(tag) != null) {
            // If the tag is used, then add a number on the front. Start with 1.
            count++;
            tag = String.format("%s%s", name.substring(0, 3), count);
        }
        
        PlayerParty party = new PlayerParty(name, tag, player, plugin);
        parties.add(party);
        return party;
    }
    
    /**
     * Reloads and refreshes the party list from the config file.
     * 
     * @param plugin The @link{ChatPartyPlugin} that manages this plugin.
     */
    public static void reloadPartiesFromConfig(ChatPartyPlugin plugin) {
        parties.clear();
        
        // Load in the parties.
        ConfigurationSection partiesConfig = plugin.getConfig().getConfigurationSection("parties");
        
        // Get the key names of the parties
        Set<String> partyNames = partiesConfig.getKeys(false);
        
        // For each party...
        for (String partyName : partyNames) {
            String tag = plugin.getConfig().getString(String.format("parties.%s.tag", partyName));
            ConfigurationSection section = plugin.getConfig().getConfigurationSection(String.format("parties.%s.players", partyName));
            Set<String> players = section.getKeys(false);
            HashMap<OfflinePlayer, PlayerPartyRank> pllist = new HashMap<OfflinePlayer, PlayerPartyRank>();
            
            // Get the players.
            for (String uuid : players) {
                UUID playerUUID = UUID.fromString(uuid);
                OfflinePlayer op = plugin.getServer().getOfflinePlayer(playerUUID);
                
                // If they have played before, add them.
                if (op.hasPlayedBefore()) {
                    String rank = section.getString(String.format("parties.%s.players.%s", partyName, uuid));
                    if (rank.equalsIgnoreCase("leader")) {
                        pllist.put(op, PlayerPartyRank.LEADER);
                    } else {
                        pllist.put(op, PlayerPartyRank.MEMBER);
                    }
                }
            }
            
            // Check that there is a leader.
            if (pllist.containsValue(PlayerPartyRank.LEADER)) {
                try {
                    parties.add(new PlayerParty(partyName, tag, pllist, plugin));
                } catch (ChatPartyException ex) {
                    plugin.getServer().getLogger().log(Level.SEVERE, null, ex);
                }
            } else {
                plugin.getServer().getLogger().log(Level.SEVERE, "The party {0} does not have a leader! Skipping creation.", partyName);
            }
        }
    }
    
    /**
     * Puts the current party data into the configuration file.
     * 
     * @param plugin The plugin with the configuration.
     */
    public static void saveConfigToFile(ChatPartyPlugin plugin) {
        // Null the party list, so that we dont have any parties that have disappeared.
        plugin.getConfig().set("parties", null);
        
        for (PlayerParty party : parties) {
            String partyName = party.partyName;
            
            // Set the tag for the party.
            plugin.getConfig().set(String.format("parties.%s.tag", partyName), party.tag);
            
            // Get the UUID list for players against, and save it to the config.
           for (OfflinePlayer op : party.members.keySet()) {
               plugin.getConfig().set(String.format("parties.%s.players.%s", partyName, op.getUniqueId().toString()), party.members.get(op));
           }
        }
    }
    
    // Constructors
    
    /**
     * Creates a player party. For use when configuration is reloaded.
     * 
     * @param partyName The name of the party.
     * @param tag The tag for the party.
     * @param players The player map.
     * @param plugin The plugin.
     */
    private PlayerParty(String partyName, String tag, Map<OfflinePlayer, PlayerPartyRank> players, ChatPartyPlugin plugin) throws ChatPartyException {
        if (getPartyFromName(partyName) != null) {
            throw new ChatPartyException(String.format("The party name %s is already in use.", partyName));
        }
        
        if (getPartyFromTag(tag) != null) {
            throw new ChatPartyException(String.format("The party tag %s is already in use.", tag));
        }
        
        this.partyName = partyName;
        this.tag = tag;
        this.plugin = plugin;
        
        Set<OfflinePlayer> playerSet = players.keySet();
        for (OfflinePlayer player : playerSet) {
            this.addPlayer(player);
            this.setPlayerRank(player, players.get(player));
        }
    }
    
    /**
     * Creates a player party. Can only be constructed by factory methods from this class.
     * 
     * @param partyName The name of the party.
     * @param tag The tag to use when displaying chat.
     * @param player The player who creates the party.
     * @param plugin The plugin.
     */
    private PlayerParty(String partyName, String tag, OfflinePlayer player, ChatPartyPlugin plugin) throws ChatPartyException {
        this.partyName = partyName;
        this.tag = tag;
        this.plugin = plugin;
        
        this.addPlayer(player);
        this.setPlayerRank(player, PlayerPartyRank.LEADER);
    }
    
    
    // Public methods
    
    /**
     * Gets the name of the party.
     * 
     * @return The party name.
     */
    public String getName() {
        return partyName;
    }
    
    /**
     * Gets the 3 letter abbreviation of the party.
     * 
     * @return The party short name.
     */
    public String getShortName() {
        return tag;
    }
    
    /**
     * Gets the members of the party.
     * 
     * @return @link{Set} of @link{OfflinePlayer}s that are members of this party.
     */
    public Set<OfflinePlayer> getPlayers() {
        return members.keySet();
    }
    
    /**
     * Gets the members of the party, filtered by rank.
     * 
     * @param rank The rank to filter on.
     * @return @link{Set} of @link{OfflinePlayer}s that are members of this party.
     */
    public Set<OfflinePlayer> getPlayers(PlayerPartyRank rank) {
        Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
        for (OfflinePlayer player : members.keySet()) {
            if (members.get(player) == rank) {
                players.add(player);
            }
        }
        
        return players;
    }
    
    /**
     * Gets the online members of the party.
     * 
     * @return @link{Set} of @link{Player}s that are members of this party.
     */
    public Set<Player> getOnlinePlayers() {
        Set<Player> p = new HashSet<Player>();
        for (OfflinePlayer op : members.keySet()) {
            if (op.isOnline()) {
                p.add(op.getPlayer());
            }
        }
        
        return p;
    }
    
    /**
     * Adds a player to a party. This will remove the player from any party they
     * are currently in.
     * 
     * @param player The @link{OfflinePlayer} to add.
     * @return <code>true</code> if successful.
     */
    public boolean addPlayer(OfflinePlayer player) {
        if (!hasPlayerPlayedBefore(player)) {
            return false;
        }
        
        // If the player is in another party, remove them. If we can't return false.
        if (PlayerParty.getPlayerParty(player) != null && !PlayerParty.getPlayerParty(player).removePlayer(player, null, PlayerRemoveReason.MOVED_PARTY)) {
            return false;
        }
        
        // Add them to the party as a member.
        members.put(player, PlayerPartyRank.MEMBER);
        addPlayerMetadata(player);
        
        return true;
    }
    
    /**
     * Adds the metadata to a player.
     * 
     * @param player The @link{Player} to add the party metadata to.
     */
    public void addPlayerMetadata(OfflinePlayer player) {
        if (player.isOnline() && members.containsKey(player)) {
            player.getPlayer().setMetadata(MetadataState.INPARTY.name(), new FixedMetadataValue(plugin, partyName));
        }
    }
    
    /**
     * Gets the player's rank in the party.
     * 
     * @param op The player to get the rank of.
     * @return The @link{PlayerPartyRank} of the player, or <code>null</code> if the player is not part of the party.
     */
    public PlayerPartyRank getPlayerRank(OfflinePlayer op) {
        return members.get(op);
    }
    
    /**
     * Sets a player's rank in the party.
     * 
     * @param player The player to set the rank of.
     * @param newRank The new rank of the player.
     * @throws ChatPartyException Thrown if the player is not in the party.
     */
    public void setPlayerRank(OfflinePlayer player, PlayerPartyRank newRank) throws ChatPartyException {
        if (!members.containsKey(player)) {
            throw new ChatPartyException(String.format("The player %s is not in this party.", player.getName()));
        }
        
        members.put(player, newRank);
    }
    
    /**
     * Removes a player from the party.
     * 
     * @param player The player to remove.
     * @param kicker The player that instigated the removal.
     * @param reason The @link{PlayerRemoveReason} that defines why the removal is occurring.
     * @return <code>true</code> if successful.
     */
    public boolean removePlayer(OfflinePlayer player, Player kicker, PlayerRemoveReason reason) {
        if (!hasPlayerPlayedBefore(player)) {
            return false;
        }
        
        if (!members.containsKey(player)) {
            if (kicker != null) {
                plugin.sendMessage(kicker, "The player " + player.getName() + " is not in your party.");
            } 
            
            return false;
        }
        
        if (kicker != null && !kicker.hasPermission("chatparty.admin") && members.get(player) == PlayerPartyRank.LEADER) {
            plugin.sendMessage(kicker, "The player " + player.getName() + " is a leader and cannot be kicked.");
        }
        
        members.remove(player);
        removePlayerMetadata(player);
        sendRemoveMessages(player, kicker, reason);
        
        if (!members.containsValue(PlayerPartyRank.LEADER)) {
            disbandParty();
        }
        
        return true;
    }
    
    /**
     * Sends a chat message.
     * 
     * @param sender The sender of the message, of <code>null</code> if the console.
     * @param message The message to send.
     */
    public void sendPlayerMessage(Player sender, String message) {
        String playerName = "*Console*";
        if (sender != null) {
            playerName = sender.getDisplayName();
        }
        
        String formattedMessage = plugin.getPartyChatTemplate().replace("{DISPLAYNAME}", playerName).replace("{PARTYNAME}", this.partyName).replace("{MESSAGE}", message);
        for (Player player : getOnlinePlayers()) {
            if (player.hasPermission("chatparty.user")) {
                player.sendMessage(formattedMessage);
            }
        }
        
        plugin.sendSpyChatMessage(this, sender, message);
    }

    /**
     * Sends a message to the party.
     * 
     * @param message The message to send.
     */
    public void sendPartyMessage(String message) {
        String msg = String.format("%s[Party] %s", plugin.getMessageColour(), message);
        for (Player player : getOnlinePlayers()) {
            if (player.hasPermission("chatparty.user")) {
                player.sendMessage(msg);
            }
        }
    }
    
    /**
     * Sends the removal messages to the relevant people.
     * 
     * @param player The player to remove.
     * @param kicker The player who kicked the player. Can be <code>null</code>
     * @param reason The reason the player was removed.
     */
    private void sendRemoveMessages(OfflinePlayer player, Player kicker, PlayerRemoveReason reason) {
        // Player
        if (player.isOnline()) {
            Player p = player.getPlayer();
            
            if (reason.equals(PlayerRemoveReason.KICKED_BY_LEADER)) {
                plugin.sendMessage(p, String.format(reason.getPlayerMessageTemplate(), kicker.getName()));
            } else {    
                plugin.sendMessage(p, reason.getPlayerMessageTemplate());
            }
        }
        
        // Admin
        if (reason.equals(PlayerRemoveReason.DISBANDED)) {
            // We don't care as an Admin.
        } else if (reason.equals(PlayerRemoveReason.KICKED_BY_LEADER)) {
            plugin.sendSpyPartyMessage(this, String.format(reason.getAdminMessageTemplate(), player.getName(), this.getName(), kicker.getName()));
        } else {
            plugin.sendSpyPartyMessage(this, String.format(reason.getAdminMessageTemplate(), player.getName(), this.getName()));
        }
    }
    
    /**
     * Disbands a party.
     * 
     * @return <code>true</code> if successful.
     */
    public boolean disbandParty() {
        Set<OfflinePlayer> playersToRemove = new HashSet<OfflinePlayer>();
        playersToRemove.addAll(members.keySet());
        
        for (OfflinePlayer p : playersToRemove) {
            removePlayer(p, null, PlayerRemoveReason.DISBANDED);
        }
        
        parties.remove(this);
        
        plugin.sendSpyPartyMessage(this, "The party " + this.getName() + " was disbanded.");
        return true;
    }
    
    /**
     * Removes the party metadata from a player if they are not in this party.
     * 
     * @param player The @link{Player} to remove the party metadata from.
     */
    public void removePlayerMetadata(OfflinePlayer player) {
        if (player.isOnline() && !members.containsKey(player)) {
            List<MetadataValue> data = player.getPlayer().getMetadata(MetadataState.INPARTY.name());
            for (MetadataValue d : data) {
                if (d.getOwningPlugin() == plugin && d.asString().equals(partyName)) {
                    
                    // Remove the metadata ONLY if the player is not in this party.
                    player.getPlayer().removeMetadata(partyName, plugin);
                }
            }
        }
    }
    
    /**
     * Checks to see if a player has played before.
     * 
     * @param player The @link{OfflinePlayer} to check.
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    private boolean hasPlayerPlayedBefore(OfflinePlayer player) {
        if (!player.hasPlayedBefore()) {
            String n = player.getName();
            if (n == null) {
                n = "(Unknown)";
            }
            
            plugin.getLogger().log(Level.SEVERE, "The player {0} has never played on this server", n);
            return false;
        }
        
        return true;
    }
    
}
