/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ChatPartyAdminCommand extends BaseCommandExecutor {

    private static final Pattern wordFilter = Pattern.compile("[a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE);
    
    public ChatPartyAdminCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    /**
     * Handles the /cpa command.
     *
     * @param cs The sender of the command.
     * @param cmnd The command that this method handles.
     * @param alias The alias that is being used.
     * @param args The arguments of the command.
     * @return <code>true</code>
     */
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadSubcommand(cs);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("addword")) {
            addWordSubcommand(cs, args[1].toLowerCase());
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("removeword")) {
            removeWordSubcommand(cs, args[1].toLowerCase());
            return true;
        }

        helpSubcommand(cs);
        return true;
    }

    private void reloadSubcommand(CommandSender cs) {
        plugin.reloadConfig();
        this.sendMessage(cs, "The configuration has been reloaded");
    }
    
    private void addWordSubcommand(CommandSender cs, String word) {
        if (!ChatPartyAdminCommand.checkFilter(word)) {
            this.sendMessage(cs, "The word must be alphanumeric.");
            return;
        }
        
        if (plugin.getNSFWChat().addBannedWord(word)) {
            this.sendMessage(cs, "The word was succesfully added to the list.");
            return;
        }
        
        this.sendMessage(cs, "The word was already in the list.");
    }

    private void removeWordSubcommand(CommandSender cs, String word) {
        if (plugin.getNSFWChat().addBannedWord(word)) {
            this.sendMessage(cs, "The word was succesfully removed from the list.");
            return;
        }
        
        this.sendMessage(cs, "The word was not in the list.");
    }

    private void helpSubcommand(CommandSender cs) {
        this.sendMessage(cs, "--- ChatParty Admin commands---");
        this.sendMessage(cs, "/cpa reload - Reloads the configuration file.");
        this.sendMessage(cs, "/cpa addword <word> - Adds a word to the NSFW list.");
        this.sendMessage(cs, "/cpa removeword <word> - Removes a word from the NSFW list.");
    }

    private static boolean checkFilter(String word) {
        return wordFilter.matcher(word).matches();
    }
    
}
