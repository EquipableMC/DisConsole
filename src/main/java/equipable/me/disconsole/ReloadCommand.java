package equipable.me.disconsole;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
	
	private final JavaPlugin plugin;
	
	public ReloadCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
		if (command.getName().equalsIgnoreCase("disconsole")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				if (sender instanceof Player player) {
					if (player.hasPermission("disconsole.reload")) {
						reloadPlugin(sender);
					} else {
						player.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
					}
				} else {
					reloadPlugin(sender);
				}
				return true;
			}
		}
		return false;
	}
	
	private void onReload() {
		DisConsole.getInstance().disableJDA();
		DisConsole.getInstance().disableLog4j();
		DisConsole.getInstance().initLog4j();
		DisConsole.getInstance().checkConfig();
		RateLimitHandler.resetRateLimiter();
	}
	
	private void reloadPlugin(CommandSender sender) {
		onReload();
		String logPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + plugin.getName() + ChatColor.DARK_GRAY + "] ";
		sender.sendMessage(  logPrefix + ChatColor.GREEN + "DisConsole has been reloaded successfully!");
		Bukkit.getConsoleSender().sendMessage(logPrefix + ChatColor.GREEN + "DisConsole has been reloaded successfully!");
	}
}
