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
package uk.co.drnaylor.charparty.party;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.enums.PlayerPartyRank;
import uk.co.drnaylor.chatparty.enums.PlayerRemoveReason;

public class PlayerParty {
    
    private final static Set<PlayerParty> parties = new HashSet<PlayerParty>();
    
    private final String partyName;
    private final Map<OfflinePlayer, PlayerPartyRank> members = new HashMap<OfflinePlayer, PlayerPartyRank>();
    private final ChatPartyPlugin plugin;
    
    
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
    // Constructors
    private PlayerParty(String partyName, ChatPartyPlugin plugin) {
        this.partyName = partyName;
        this.plugin = plugin;
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
        return partyName.substring(0, 2);
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
        
        members.remove(player);
        removePlayerMetadata(player);
        sendRemoveMessages(player, kicker, reason);
        
        if (!members.containsValue(PlayerPartyRank.LEADER)) {
            disbandParty();
        }
        
        return true;
    }
    
    /**
     * Sends the removal messages to the relavent people.
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
                plugin.sendMessage(p, String.format(reason.getAdminMessageTemplate(), kicker.getName()));
            } else {    
                plugin.sendMessage(p, reason.getPlayerMessageTemplate());
            }
        }
        
        // Admin
        if (reason.equals(PlayerRemoveReason.DISBANDED)) {
            // We don't care as an Admin.
            return;
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
