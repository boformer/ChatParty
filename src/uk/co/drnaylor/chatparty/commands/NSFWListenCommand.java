package uk.co.drnaylor.chatparty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;

/**
 * A class that represents the /listennsfw command
 */
public class NSFWListenCommand extends BaseCommandExecutor{

	public NSFWListenCommand(ChatPartyPlugin plugin) {
		super(plugin);
	}

	/**
	 * Handles the /listennsfw command
	 * 
	 * @param sender The sender of this command
	 * @param cmd The command class
	 * @param label The actual command typed by the user
	 * @param args The arguments of the command
	 * @return <code>true</code>
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		if (sender instanceof Player) {
			Player pla = (Player) sender; 
			if (plugin.toggleNSFWListening(pla)) {
				plugin.sendMessage(pla, "NSFW Chat WILL be displayed from now on.");
			} else {
				plugin.sendMessage(pla, "NSFW Chat WILL NOT be displayed from now on.");
			}
		} else {
			sender.sendMessage("The console cannot toggle NSFW Chat channel");
		}
		return true;
	}

}
