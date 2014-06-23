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

package uk.co.drnaylor.chatparty.enums;

/**
 * Provides reasons for why a player is removed from a party.
 */
public enum PlayerRemoveReason {
    
    /**
     * Left of own accord.
     */
    LEFT("You left the party.", "%s left the party %s"),
    
    /**
     * Kicked by a leader of the party.
     */
    KICKED_BY_LEADER("You were kicked from the party by %s.", "%s was kicked from the party %s by %s"),
    
    /**
     * Kicked by an admin of the party.
     */
    KICKED_BY_ADMIN("You were kicked from the party by an admin.", "%s was kicked from the party %s by an admin"),
    
    /**
     * Moved to another party.
     */
    MOVED_PARTY("You were moved to a different party.", "%s was moved to a different party."),
    
    /**
     * The party was disbanded.
     */
    DISBANDED("You were kicked from the party as it is disbanding.", "%s was kicked from the party %s as it is disbanding"),
    
    /**
     * Any other reason.
     */
    OTHER("You were kicked from the party.", "%s was kicked from the party %s"),;
    
    // Class
    private final String playerMessageTemplate;
    private final String adminMessageTemplate;
    
    PlayerRemoveReason(String playerMessageTemplate, String adminMessageTemplate) {
        this.playerMessageTemplate = playerMessageTemplate;
        this.adminMessageTemplate = adminMessageTemplate;
    }
    
    public String getPlayerMessageTemplate() {
        return playerMessageTemplate;
    }
    
    public String getAdminMessageTemplate() {
        return adminMessageTemplate;
    }
}
