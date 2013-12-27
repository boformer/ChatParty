package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import com.github.schmidtbochum.chatparty.Party;
import com.github.schmidtbochum.chatparty.Party.MemberType;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        if (args.length == 3 && args[0].equalsIgnoreCase("rm")) {
            removeSubcommand(cs, args[1], args[2]);
            return true;
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            addSubcommand(cs, args[1], args[2]);
            return true;
        }
        
        this.helpSubcommand(cs);
        return true;
    }

    private void helpSubcommand(CommandSender cs) {
        sendMessage(cs, "--- ChatParty Admin Commands ---");
        sendMessage(cs, "/pa add <player> <party> - Add online player to party");
        sendMessage(cs, "/pa rm <player> - Remove online player from current party");
        sendMessage(cs, "/pa rm <player> <party> - Remove player from party");
        sendMessage(cs, "/pa lead <player> - Add online player as leader of party");
        sendMessage(cs, "/pa disband <party> - Disband party");
        sendMessage(cs, "/pa list - List all parties and their members");
        sendMessage(cs, "/pac  <party> <chat> - Chat to party");
    }

    private void disbandSubcommand(CommandSender cs, String partyName) {
        Party party = plugin.loadParty(partyName);

        if (party == null) {
            sendMessage(cs, "That party does not exist.");
            return;
        }

        party.disband();
        sendMessage(cs, "The party has been disbanded.");
    }

    private void listSubcommand(CommandSender cs) {

        sendMessage(cs, "--- Chat Parties ---");
        sendMessage(cs, "A star indicates a leader of the party.");
        sendMessage(cs, "----------");

        // Get the active parties.
        Map<String, Party> parties = plugin.getActiveParties();

        // For each party, list the players.
        // Green for online, Grey for offline
        for (Party p : parties.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.name).append(": ");

            Map<MemberType, List<String>> pls = p.getMembers();

            createPlayerString(sb, pls.get(MemberType.LEADER), true);
            createPlayerString(sb, pls.get(MemberType.MEMBER), false);

            cs.sendMessage(sb.toString());
        }
    }

    private void leadSubcommand(CommandSender cs, String playername) {
        Player player = plugin.getServer().getPlayerExact(playername);

        if (player == null) {
            sendMessage(cs, "That player is not online.");
            return;
        }

        Party party = plugin.getPlayerParty(player);

        if (party == null) {
            sendMessage(cs, "That player is not part of a party.");
            return;
        }
        
        party.addLeader((OfflinePlayer) player);
    }
    
    /**
     * Processes the rm subcommand. This is the version for use with online
     * players.
     * 
     * @param cs The sender of the command.
     * @param playerName The player to remove from their party.
     */
    private void removeSubcommand(CommandSender cs, String playerName) {
        Player player = plugin.getServer().getPlayerExact(playerName);
        
        if (player == null) {
            sendMessage(cs, String.format("That player is not online. Use /pa rm %s <party> instead if you know their party.", playerName));
            return;
        }
        
        Party party = plugin.getPlayerParty(player);
        
        if (party == null) {
            sendMessage(cs, "That player is not in any party.");
            return;
        }
        
        party.removePlayer(player, true);
    }
    
    /**
     * Processes the rm subcommand. This is the version for use with any
     * players.
     * 
     * @param cs The sender of the command.
     * @param playerName The player to remove from their party.
     * @param partyName The party to remove the player from.
     */
    private void removeSubcommand(CommandSender cs, String playerName, String partyName) {
        Player player = plugin.getServer().getPlayerExact(playerName);
        
        if (player != null) {
            Party party = plugin.getPlayerParty(player);
            if (party.name.equalsIgnoreCase(partyName)) {
                party.removePlayer(player, true);
            }
            
            sendMessage(cs, "That player is not in the party specified.");
            return;
        }
        
        Party party = plugin.loadParty(partyName);
        
        if (party == null) {
            sendMessage(cs, "That party does not exist.");
            return;
        }
        
        List<String> members = party.getMembers().get(MemberType.LEADER);
        members.addAll(party.getMembers().get(MemberType.MEMBER));
        
        for (String s : members) {
            if (s.equalsIgnoreCase(playerName)) {
                party.removePlayer(s);
                sendMessage(cs, "The player was removed from the party.");
                return;
            }
        }
        
        sendMessage(cs, "The player was not in that party.");
    }
    
    /**
     * Processes the add subcommand.
     * 
     * @param cs The sender of the command.
     * @param playerName The player to add to a party.
     * @param partyName The party to add the player to.
     */
    private void addSubcommand(CommandSender cs, String playerName, String partyName) {
        Player player = plugin.getServer().getPlayerExact(playerName);
        
        if (player != null) {
            sendMessage(cs, "That player is not online.");
            return;
        }
        
        Party current = plugin.getPlayerParty(player);
        if (current != null) {
            sendMessage(cs, String.format("%s is currently in the party %s. Remove them before adding them to a new party.", playerName, partyName));
        }
        
        Party party = plugin.loadParty(partyName);
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
    private void createPlayerString(StringBuilder sb, List<String> players, boolean isLeader) {
        for (String player : players) {

            if (plugin.getServer().getPlayerExact(player) != null) {
                sb.append(ChatColor.GREEN);
            } else {
                sb.append(ChatColor.GRAY);
            }

            if (isLeader) {
                sb.append("*");
            }

            sb.append(player).append(" ");
        }
    }

}
