package supercheese200.NSFWChat;
// Built off of uk.co.drnaylor.chatparty.admin.AdminChat
import com.github.schmidtbochum.chatparty.ChatPartyPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NSFWChat {
    private final ChatPartyPlugin plugin;
    public NSFWChat(ChatPartyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sends a message to the NSFW chat channel
     * 
     * @param sender The sender of the message, or null if sent from Console
     * @param message The message to send
     */
    public void sendNSFWMessage(Player sender, String message) {
        String tag = "*Console*";
        if (sender != null) {
            tag = sender.getDisplayName();
        }
        
        String formattedMessage = plugin.getNSFWChatTemplate().replace("{DISPLAYNAME}", tag).replace("{MESSAGE}", message);
    	for (Player pla : Bukkit.getServer().getOnlinePlayers()) {
    		if (pla.hasMetadata("nsfwlistening")) {
    			pla.sendMessage(formattedMessage);
    		}
    	}
    	Bukkit.getServer().getConsoleSender().sendMessage(formattedMessage);
    }
}
