/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.drnaylor.chatparty.enums;

/**
 * Enumerations to avoid the use of magic strings in Metadata.
 */
public enum MetadataState {
    /**
     * Player is in party chat.
     */
    PARTYCHAT,
    
    /**
     * Player is in party.
     */
    INPARTY,
    
    /**
     * Player is leader of current party.
     */
    PARTYLEADER,
    
    /**
     * Player is talking in admin chat.
     */
    ADMINCHAT,
    
    /**
     * Player is talking in NSFW chat.
     */
    NSFWCHAT,
    
    /**
     * Player is listening to the NSFW channel.
     */
    NSFWLISTENING,
    
    /**
     * Player has toggled Global Chat off.
     */
    GLOBALCHATOFF,
    
    /**
     * Player has an invitation from a party.
     */
    PARTYINVITE,
    
    /**
     * Player should be ignored by the party chat filter (but not the
     * NSFW chat filter) when they next speak.
     */
    IGNORE
}
