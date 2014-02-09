package uk.co.drnaylor.chatparty.commands;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;
import com.github.schmidtbochum.chatparty.Party;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import uk.co.drnaylor.chatparty.util.Utilities;


public class PartyAdminChatCommand extends BaseCommandExecutor implements TabCompleter {

    public PartyAdminChatCommand(ChatPartyPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if (args.length < 2) {
            sendMessage(cs, "Usage: /pc <party> <message>");
            return true;
        }
        
        Party party = plugin.loadParty(args[0]);
        
        if (party == null) {
            // Party does not exist.
            sendMessage(cs, "That party does not exist.");
            return true;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);
            if (i+1 < args.length) {
                sb.append(" ");
            }
        }

        party.sendPlayerMessage(this.getPlayerFromSender(cs), sb.toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] args) {
        if (args.length != 1) {
            return null;
        }
        
        if (args[0] == null) {
            return filterNames(null);
        }
        
        return filterNames(args[0]);
    }
    
    private List<String> filterNames(String request) {
        List<String> list = Utilities.asSortedList(new ArrayList<String>(plugin.getActiveParties().keySet()));
        
        if (request == null || "".equals(request) || list.isEmpty()) {
            return list;
        }
        
        // Get the iterator
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String n = it.next();
            
            // Remove anything that does not match
            if (!n.toLowerCase().startsWith(request.toLowerCase())) {
                it.remove();
            }
        }
        
        return list;
    }
}
