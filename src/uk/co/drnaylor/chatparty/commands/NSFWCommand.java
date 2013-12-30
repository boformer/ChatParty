package uk.co.drnaylor.chatparty.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.schmidtbochum.chatparty.ChatPartyPlugin;

/**
 *
 * A class that represents the /nsfw command
 */
public class NSFWCommand extends BaseCommandExecutor{

	public NSFWCommand(ChatPartyPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param sender The sender of the command
	 * @param cmd The command class
	 * @param label The actual string the user entered
	 * @param args The arguments of the command
	 * @return <code>true</code>
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length >= 1) {
			directToNSFWChat(this.getPlayerFromSender(sender), args);
		} else {
			if (sender instanceof Player) {
				Player pla = getPlayerFromSender(sender);
				if (plugin.toggleNSFWChat(pla)) {
					if (pla.hasMetadata("nsfwlistening")) {
					plugin.sendMessage(pla, "NSFW Chat is ON");
					} else {
						plugin.sendMessage(pla, "You cannot chat in the NSFW chat channel without listening to it! Type /nsfwlisten to listen.");
					}
				} else {
					plugin.sendMessage(pla, "NSFW Chat is OFF");
				}
			} else {
				sender.sendMessage("You cannot toggle NSFW Chat from console, please use /nsfwchat <msg>");
			}
		}
		return true;
	}
	
	private void directToNSFWChat(Player player, String[] args) {
		StringBuilder s = new StringBuilder();
		for (String st : args) {
			if (s.length() > 0) {
				s.append(" ");
			}

			s.append(st);
		}

		plugin.getNSFWChat().sendNSFWMessage(player, s.toString());
	}
}
