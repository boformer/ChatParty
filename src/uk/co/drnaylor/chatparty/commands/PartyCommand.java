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
package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import uk.co.drnaylor.chatparty.enums.MetadataState;
import uk.co.drnaylor.chatparty.enums.PlayerPartyRank;
import uk.co.drnaylor.chatparty.enums.PlayerRemoveReason;
import uk.co.drnaylor.chatparty.exceptions.ChatPartyException;
import uk.co.drnaylor.chatparty.party.PlayerParty;

public class PartyCommand extends BaseCommandExecutor {

    public PartyCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Processes the /party command.
     *
     * @param cs The sender of the command.
     * @param cmnd The command this object represents.
     * @param string The actual command used.
     * @param args The arguments sent.
     * @return <code>true</code> if successful. <code>false</code> otherwise.
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        try {
            Player player = this.getPlayerFromSender(cs);
            if (player == null) {
                return false;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("join")) {
                joinSubcommand(player);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
                leaveSubcommand(player);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("members")) {
                membersSubcommand(player);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("spy")) {
                spySubcommand(player);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                toggleSubcommand(player);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
                inviteSubcommand(player, args[1]);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
                createSubcommand(player, args[1]);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("leader")) {
                leaderSubcommand(player, args[1]);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
                kickSubcommand(player, args[1]);
                return true;
            }

            helpSubcommand(player);
        } catch (ChatPartyException ex) {
            if (cs instanceof Player) {
                plugin.sendMessage((Player) cs, ex.getMessage());
            } else {
                cs.sendMessage(ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Provides the text for the /party help subcommand
     *
     * @param player The player to send the information to.
     */
    private void helpSubcommand(Player player) {

        plugin.sendMessage(player, "--- ChatParty Help ---");

        plugin.sendMessage(player, "/chat" + ChatColor.WHITE + ": Toggle the public chat.");

        PlayerParty party = PlayerParty.getPlayerParty(player);
        
        if (party != null) {
            plugin.sendMessage(player, "/p <message>" + ChatColor.WHITE + ": Send a message to your party");
            plugin.sendMessage(player, "/party leave" + ChatColor.WHITE + ": Leave your party");
            plugin.sendMessage(player, "/party members" + ChatColor.WHITE + ": Show the member list");
            plugin.sendMessage(player, "/party toggle" + ChatColor.WHITE + ": Toggle the party chat");
            if (party.getPlayerRank(player) == PlayerPartyRank.LEADER && player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "/party invite <player>" + ChatColor.WHITE + ": Invite a player to your party");
                plugin.sendMessage(player, "/party kick <player>" + ChatColor.WHITE + ": Kick a player from your party");
                //sendMessage(player, "/party name <name>" + ChatColor.WHITE + ": Rename your party.");
                plugin.sendMessage(player, "/party leader <player>" + ChatColor.WHITE + ": Add a leader to your party");
            }
        } else {
            plugin.sendMessage(player, "/party join" + ChatColor.WHITE + ": Accept a party invitation");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "/party create <name>" + ChatColor.WHITE + ": Create a new chat party");
            }
        }
        if (player.hasPermission("chatparty.admin")) {
            plugin.sendMessage(player, "/party spy" + ChatColor.WHITE + ": Toggle messages from all parties.");
        }
    }

    /**
     * Runs on /party join.
     *
     * @param player The player involved.
     */
    private void joinSubcommand(Player player) {
        if (!player.hasMetadata(MetadataState.PARTYINVITE.name())) {
            noInvitation(player);
            return;
        }

        // Get the party of the name in the Metadata.
        String partyName = player.getMetadata(MetadataState.PARTYINVITE.name()).get(0).asString();
        PlayerParty party = PlayerParty.getPartyFromName(partyName);

        if (party == null) {
            noInvitation(player);
            return;
        }

        //CONDITIONS END
        party.addPlayer(player);
    }

    /**
     * Leave the current party.
     *
     * @param player The player that is leaving their party.
     */
    private void leaveSubcommand(Player player) {
        PlayerParty party = PlayerParty.getPlayerParty(player);

        if (party == null) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }

            return;
        }

        //CONDITIONS END
        party.removePlayer(player, null, PlayerRemoveReason.LEFT);
    }

    /**
     * Gives a player an invite
     *
     * @param player The player that is inviting
     * @param invited The player being invited
     */
    private void inviteSubcommand(Player player, String invited) {

        if (!player.hasPermission("chatparty.leader")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        PlayerParty party = PlayerParty.getPlayerParty(player);

        if (party == null) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }

            return;
        }

        if (party.getPlayerRank(player) != PlayerPartyRank.LEADER) {
            plugin.sendMessage(player, "Only party leaders can invite other players.");
            return;
        }

        Player invitedPlayer = plugin.getServer().getPlayer(invited);

        if (invitedPlayer == null || !invitedPlayer.isOnline()) {
            plugin.sendMessage(player, "You can only invite online players.");
            return;
        }

        if (!invitedPlayer.hasPermission("chatparty.user")) {
            plugin.sendMessage(player, "The player does not have the permission for the party system.");
            return;
        }

        PlayerParty otherParty = PlayerParty.getPlayerParty(invitedPlayer);
        if (otherParty != null) {
            plugin.sendMessage(player, "The player is already in a party.");
            return;
        }

        invitedPlayer.setMetadata(MetadataState.PARTYINVITE.name(), new FixedMetadataValue(plugin, party.getName()));

        plugin.sendMessage(player, "You invited " + invitedPlayer.getName() + " to your party.");

        plugin.sendMessage(invitedPlayer, player.getName() + " invited you to the party \"" + party.getName() + "\".");
        plugin.sendMessage(invitedPlayer, "To accept the invitation, type /party join");
    }

    /**
     * Creates a party.
     *
     * @param player The player that created the party.
     * @param partyName The name of the party.
     */
    private void createSubcommand(Player player, String partyName) throws ChatPartyException {
        if (!player.hasPermission("chatparty.leader")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        if (PlayerParty.getPlayerParty(player) != null) {
            plugin.sendMessage(player, "You are already in a party.");
            return;
        }

        if (partyName.length() > 15) {
            plugin.sendMessage(player, "This name is too long! (4-15 letters)");
            return;
        }
        if (partyName.length() < 4) {
            plugin.sendMessage(player, "This name is too short! (4-15 letters)");
            return;
        }

        if (PlayerParty.getPartyFromName(partyName) != null) {
            plugin.sendMessage(player, "The party \"" + partyName + "\" already exists. Please choose a different name.");
            return;
        }

        PlayerParty.createPlayerParty(player, partyName, plugin);
        
        plugin.sendMessage(player, String.format("The party %s has been created.", partyName));
        plugin.sendMessage(player, "To invite players to your party, type /party invite <playername>");
    }

    /**
     * Promotes a user to a leader.
     *
     * @param player The player who is promoting another player.
     * @param toPromote The name of the player to promote.
     */
    private void leaderSubcommand(Player player, String toPromote) throws ChatPartyException {
        if (!player.hasPermission("chatparty.leader")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        PlayerParty party = PlayerParty.getPlayerParty(player);

        if (party == null) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }
            return;
        }

        if (party.getPlayerRank(player) != PlayerPartyRank.LEADER) {
            plugin.sendMessage(player, "Only party leaders can promote other players.");
            return;
        }

        OfflinePlayer promotedPlayer = plugin.getServer().getOfflinePlayer(toPromote);
        if (!promotedPlayer.hasPlayedBefore()) {
            plugin.sendMessage(player, "The player has never played on this server!");
            return;
        }

        PlayerPartyRank rank = party.getPlayerRank(promotedPlayer);
        if (rank == null) {
            plugin.sendMessage(player, "The player is not a member of your party.");
            return;
        } else if (rank == PlayerPartyRank.MEMBER) {
            plugin.sendMessage(player, "The player is already a leader.");
            return;
        }

        //CONDITIONS END
        party.setPlayerRank(promotedPlayer, PlayerPartyRank.LEADER);
    }

    private void kickSubcommand(Player player, String playerName) {
        if (!player.hasPermission("chatparty.leader")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        PlayerParty party = PlayerParty.getPlayerParty(player);

        if (party == null) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }
            return;
        }

        if (party.getPlayerRank(player) != PlayerPartyRank.LEADER) {
            plugin.sendMessage(player, "You must be the leader of the party to kick players.");
            return;
        }

        OfflinePlayer kickedPlayer = plugin.getServer().getOfflinePlayer(playerName);
        if (!party.getPlayers().contains(kickedPlayer)) {
            plugin.sendMessage(player, "That player is not in your party.");
        }

        party.removePlayer(kickedPlayer, player, PlayerRemoveReason.KICKED_BY_LEADER);
    }

    private void membersSubcommand(Player player) {
        if (!player.hasPermission("chatparty.user")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        PlayerParty party = PlayerParty.getPlayerParty(player);

        if (party == null) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }
            return;
        }

        //CONDITIONS END
        String sep = ", ";

        Set<OfflinePlayer> leaders = party.getPlayers(PlayerPartyRank.LEADER);
        Set<OfflinePlayer> members = party.getPlayers(PlayerPartyRank.MEMBER);

        StringBuilder builder = new StringBuilder();
        for (OfflinePlayer p : leaders) {
            if (builder.length() > 0) {
                builder.append(sep);
            }

            builder.append(p.getName());
        }

        String l = builder.toString();

        builder = new StringBuilder();
        for (OfflinePlayer p : members) {
            if (builder.length() > 0) {
                builder.append(sep);
            }
            builder.append(p.getName());
        }

        String m = builder.toString();

        plugin.sendMessage(player, "Member List of the party \"" + party.getName() + "\":");
        plugin.sendMessage(player, "Leaders (" + leaders.size() + "): " + l);
        plugin.sendMessage(player, "Members (" + members.size() + "): " + m);
    }

    private void toggleSubcommand(Player player) {
        if (!player.hasPermission("chatparty.user")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        if (!player.hasMetadata(MetadataState.INPARTY.name())) {
            plugin.sendMessage(player, "You are not in a party.");
            if (player.hasPermission("chatparty.leader")) {
                plugin.sendMessage(player, "Create your own party with /party create <name>.");
            }
            return;
        }

        //CONDITIONS END
        boolean enabled = plugin.togglePartyChat(player);

        if (enabled) {
            plugin.sendMessage(player, "Party Chat is now ON");
        } else {
            plugin.sendMessage(player, "Party Chat is now OFF");
        }
    }

    /**
     * Toggles spy mode on or off.
     *
     * @param player The player to affect.
     */
    private void spySubcommand(Player player) {
        if (!player.hasPermission("chatparty.admin")) {
            plugin.sendMessage(player, "You do not have access to that command.");
            return;
        }

        boolean enabled = plugin.toggleSpy(player);

        if (enabled) {
            plugin.sendMessage(player, "You enabled party spy mode.");
        } else {
            plugin.sendMessage(player, "You disabled party spy mode.");
        }
    }

    /**
     * Prints a message to the user if they don't have an invitation to join a
     * party.
     *
     * @param player The player to send the message to.
     */
    private void noInvitation(Player player) {
        plugin.sendMessage(player, "No active party invitation.");
        if (player.hasPermission("chatparty.leader")) {
            plugin.sendMessage(player, "Create your own party with /party create <name>.");
        }
    }

}
