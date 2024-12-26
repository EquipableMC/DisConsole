package equipable.me.disconsole;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class MessageReceived extends ListenerAdapter {
	
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		String discordChannel = DisConsole.getInstance().getConfig().getString("Log-Channel");
		String command = String.valueOf(event.getMessage());
        if (discordChannel.equals(event.getChannel().getId()) && (!event.getAuthor().isBot())) {
			Bukkit.getScheduler().callSyncMethod(DisConsole.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
		}
	}
}
