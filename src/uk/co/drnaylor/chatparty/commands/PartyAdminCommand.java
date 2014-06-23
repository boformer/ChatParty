package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.drnaylor.chatparty.enums.PlayerPartyRank;
import uk.co.drnaylor.chatparty.enums.PlayerRemoveReason;
import uk.co.drnaylor.chatparty.exceptions.ChatPartyException;
import uk.co.drnaylor.chatparty.party.PlayerParty;

/**
 * Provides commands for managing parties as an admin.
 */
public class PartyAdminCommand extends BaseCommandExecutor {

    public PartyAdminCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /pa command.
     *
     * @param cs The sender of the command.
     * @param cmnd The command.
     * @param string The alias that is used.
     * @param args The arguments to the command.
     * @return <code>true</code> if successful.
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {

        try {
            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                listSubcommand(cs);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("disband")) {
                disbandSubcommand(cs, args[1]);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("lead")) {
                leadSubcommand(cs, args[1]);
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("rm")) {
                removeSubcommand(cs, args[1]);
                return true;
            }

            if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
                addSubcommand(cs, args[1], args[2]);
                return true;
            }

            this.helpSubcommand(cs);

        } catch (ChatPartyException ex) {
            if (cs instanceof Player) {
                plugin.sendMessage((Player) cs, ex.getMessage());
            } else {
                cs.sendMessage(ex.getMessage());
            }
        }

        return true;
    }

    private void helpSubcommand(CommandSender cs) {
        sendMessage(cs, "--- ChatParty Admin Commands ---");
        sendMessage(cs, "/pa add <player> <party> - Add online player to party");
        sendMessage(cs, "/pa rm <player> - Remove online player from current party");
        sendMessage(cs, "/pa rm <player> <party> - Remove player from party");
        sendMessage(cs, "/pa lead <player> - Add player as leader of party");
        sendMessage(cs, "/pa disband <party> - Disband party");
        sendMessage(cs, "/pa list - List all parties and their members");
        sendMessage(cs, "/pac  <party> <chat> - Chat to party");
    }

    private void disbandSubcommand(CommandSender cs, String partyName) {
        PlayerParty party = PlayerParty.getPartyFromName(partyName);

        if (party == null) {
            sendMessage(cs, "That party does not exist.");
            return;
        }

        party.disbandParty();
        sendMessage(cs, "The party has been disbanded.");
    }

    private void listSubcommand(CommandSender cs) {

        sendMessage(cs, "--- Chat Parties ---");
        sendMessage(cs, "A star indicates a leader of the party.");
        sendMessage(cs, "--------------------");

        // Get the active parties.
        Set<PlayerParty> parties = PlayerParty.getParties();

        // For each party, list the players.
        // Green for online, Grey for offline
        for (PlayerParty p : parties) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getName()).append(": ");

            createPlayerString(sb, p.getPlayers(PlayerPartyRank.LEADER), true);
            createPlayerString(sb, p.getPlayers(PlayerPartyRank.MEMBER), false);

            cs.sendMessage(sb.toString());
        }
    }

    private void leadSubcommand(CommandSender cs, String playername) throws ChatPartyException {
        OfflinePlayer op = plugin.getServer().getOfflinePlayer(playername);
        if (!op.hasPlayedBefore()) {
            sendMessage(cs, "That player has never played before.");
            return;
        }

        PlayerParty party = PlayerParty.getPlayerParty(op);

        if (party == null) {
            sendMessage(cs, "That player is not part of a party.");
            return;
        }

        party.setPlayerRank(op, PlayerPartyRank.LEADER);
    }

    /**
     * Processes the rm subcommand.
     *
     * @param cs The sender of the command.
     * @param playerName The player to remove from their party.
     */
    private void removeSubcommand(CommandSender cs, String playerName) {
        
        OfflinePlayer op = plugin.getServer().getOfflinePlayer(playerName);
        if (!op.hasPlayedBefore()) {
            sendMessage(cs, "That player has never played before.");
            return;
        }
        
        PlayerParty party = PlayerParty.getPlayerParty(op);

        if (party == null) {
            sendMessage(cs, "That player is not in any party.");
            return;
        }

        if (!party.removePlayer(op, null, PlayerRemoveReason.KICKED_BY_ADMIN)) {
            sendMessage(cs, "The player could not be removed from the party.");
        }
    }

    /**
     * Processes the add subcommand.
     *
     * @param cs The sender of the command.
     * @param playerName The player to add to a party.
     * @param partyName The party to add the player to.
     */
    private void addSubcommand(CommandSender cs, String playerName, String partyName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);

        if (!player.hasPlayedBefore()) {
            sendMessage(cs, "That player has never played before.");
            return;
        }

        PlayerParty current = PlayerParty.getPlayerParty(player);
        if (current != null) {
            sendMessage(cs, String.format("%s is currently in the party %s. Remove them before adding them to a new party.", playerName, partyName));
            return;
        }

        PlayerParty party = PlayerParty.getPartyFromName(partyName);
        if (party == null) {
            sendMessage(cs, "That party does not exist.");
            return;
        }

        party.addPlayer(player);
    }

    /**
     * Creates a player string and appends it to a StringBuilder.
     *
     * @param sb The StringBuilder to append it to.
     * @param player The name of the player.
     * @param isLeader Whether the player is a leader or not.
     */
    private void createPlayerString(StringBuilder sb, Set<OfflinePlayer> players, boolean isLeader) {
        for (OfflinePlayer player : players) {

            if (player.isOnline()) {
                sb.append(ChatColor.GREEN);
            } else {
                sb.append(ChatColor.GRAY);
            }

            if (isLeader) {
                sb.append("*");
            }

            sb.append(player.getName()).append(" ");
        }
    }

}
