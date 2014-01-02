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
package uk.co.drnaylor.chatparty.interfaces;

import com.github.schmidtbochum.chatparty.Party;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import supercheese200.NSFWChat.NSFWChat;
import uk.co.drnaylor.chatparty.admin.AdminChat;

/**
 *
 * @author Daniel
 */
public interface IChatPartyPlugin {

    /**
     * Gets the active parties.
     *
     * @return The active parties.
     */
    Map<String, Party> getActiveParties();

    /**
     * Gets the admin chat class.
     *
     * @return The class.
     */
    AdminChat getAdminChat();

    /**
     * Gets the admin chat template from the config file.
     *
     * @return A string representation of the template.
     */
    String getAdminChatTemplate();

    /**
     * Gets whether to invert the chat state on /p
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    boolean getInvertP();

    /**
     * Gets the ChatColor to use with messages.
     *
     * @return The ChatColor.
     *
     * @see ChatColor
     */
    ChatColor getMessageColour();

    /**
     * Gets the NSFW chat class
     *
     * @return The NSFW chat class
     */
    NSFWChat getNSFWChat();

    /**
     * Gets the NSFW chat template from the config file.
     *
     * @return A string representation of the template
     */
    String getNSFWChatTemplate();

    /**
     * Gets the party chat template from the config file.
     *
     * @return A string representation of the template.
     */
    String getPartyChatTemplate();

    /**
     * Gets the party a player belongs to.
     *
     * @param player The player.
     * @return The party that player is part of, or null if not in a party.
     */
    Party getPlayerParty(Player player);

    /**
     * Gets whether to toggle with p.
     *
     * @return <code>true</code> if we toggle with /p, <code>false</code>
     * otherwise.
     */
    boolean getToggleWithP();

    /**
     * Gets a party based on it's name.
     *
     * @param name The name of the party.
     * @return The party object.
     *
     * @see Party
     */
    Party loadParty(String name);

    /**
     * Registers a player as a spy.
     *
     * @param player The player to register as a spy.
     */
    void registerSpy(Player player);

    /**
     * Reloads the config file, and sets up the banned word list.
     *
     * This method overrides standard Bukkit behaviour.
     */
    void reloadConfig();

    /**
     * Removes a player from the system.
     *
     * @param playerName The player to remove.
     */
    void removePlayer(String playerName);

    /**
     * Saves a party.
     *
     * @param party The party to save the data for.
     */
    void saveParty(Party party);

    /**
     * Saves a player's data.
     *
     * @param player The player to save.
     */
    void savePlayer(Player player);

    /**
     * Sends a message to the selected player.
     *
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    void sendMessage(Player player, String message);

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
    void sendSpyChatMessage(Party party, Player sender, String message);

    /**
     * Handles the Party Spy message sending.
     *
     * This method will take messages to be sent to the spies and send them out
     * with the correct formatting.
     *
     * @param party The party that sent the message.
     * @param message The message to send.
     */
    void sendSpyPartyMessage(Party party, String message);

    /**
     * Toggles a user's admin chat status.
     *
     * @param player The player to toggle admin chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    boolean toggleAdminChat(Player player);

    /**
     * Toggles a user's global chat status.
     *
     * @param player The player to toggle global chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    boolean toggleGlobalChat(Player player);

    /**
     * Toggles a user's NSFW chat status.
     *
     * @param player The player to toggle NSFW chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    boolean toggleNSFWChat(Player player);

    /**
     * Toggles whether a user is listening to the NSFW chat channel.
     *
     * @param pla The player to toggle NSFW chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    boolean toggleNSFWListening(Player pla);

    /**
     * Toggles a user's party chat status.
     *
     * @param player The player to toggle party chat for.
     * @return <code>true</code> if turned on, <code>false</code> otherwise.
     */
    boolean togglePartyChat(Player player);

    /**
     * Toggles a player's spy state.
     *
     * @param player The player to toggle.
     * @return <code>true</code> if the player now has spy, <code>false</code>
     * otherwise.
     */
    boolean toggleSpy(Player player);

    /**
     * Unregisters a player as a spy.
     *
     * @param player The player to remove from the spy list.
     */
    void unregisterSpy(Player player);
    
    /**
     * Returns the Bukkit server object.
     * 
     * @return The server.
     */
    Server getServer();
}
