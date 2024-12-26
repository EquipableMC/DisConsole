package equipable.me.disconsole;

import equipable.me.disconsole.utils.Utilities;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collections;
import java.util.Objects;

public class DisConsole extends JavaPlugin {
    private static DisConsole instance;

    private static JDA jda;

    public static JDA getJDA() {
        return jda;
    }

    @Override
    public void onEnable() {
        instance = this;
        initializeConfig();
        initLog4j();
        checkConfig();
        if (!this.isEnabled()) {
            return;
        }
        Utilities.log(ChatColor.GREEN + "DisConsole plugin has been enabled!");

        int pluginID = 24238;
        Metrics metrics = new Metrics(this, pluginID);
    }
    
    public static DisConsole getInstance() {
        return instance;
    }

    public void checkConfig() {
        String botToken = getConfig().getString("Bot-Token");

        if (botToken == null || botToken.isEmpty()) {
            getLogger().severe("Invalid Bot Token. Please specify a valid bot token in the config, then restart the server!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        String discordChannelId = getConfig().getString("Log-Channel");
        if (discordChannelId == null || discordChannelId.isEmpty()) {
            getLogger().warning("Log-Channel is not set in the config.");
            getLogger().warning("Please set the Log-Channel in the config.yml and restart the server for the changes to take effect.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(botToken)
                    .setStatus(OnlineStatus.ONLINE)
                    .enableIntents(
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setAutoReconnect(true)
                    .addEventListeners(new MessageReceived())
                    .build();
            jda.awaitReady();
            Utilities.log(ChatColor.GREEN + "JDA has been initialized successfully.");
        } catch (InvalidTokenException e) {
            getLogger().severe("Invalid Bot Token: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (InterruptedException e) {
            getLogger().severe("JDA initialization was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (Exception e) {
            getLogger().severe("Unexpected error while initializing JDA: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    
    public void initializeConfig() {
        saveConfig();
        getConfig().addDefault("Bot-Token", "");
        getConfig().addDefault("Log-Channel", "");
        getConfig().addDefault("Allow-Discord-Command-Execution", false);
        getConfig().options().setHeader(
	        Collections.singletonList("""
                    Configurations for the Discord bot integration.\s
                    # Bot-Token: The token for your discord bot.\s
                    # Log-Channel: The id of the discord channel the bot will send the log messages in.
                    # allow-discord-command-execution: A config option for enabling or disabling the execution of minecraft commands through discord. Disabled by default. Accepted values are true or false.""")
        );
        getConfig().options().copyDefaults(true);
        saveConfig();
    }


    public void initLog4j() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        
        // Create the custom appender
        LogAppender customAppender = new LogAppender();
        customAppender.start();
        
        // Add the custom appender to the root logger
        LoggerConfig loggerConfig = config.getRootLogger();
        loggerConfig.addAppender(customAppender, null, null);
        
        // Update the Log4j configuration
        context.updateLoggers();
    }

    public static void sendMessage(String msg) {
        String DISCORD_CHANNEL = DisConsole.getInstance().getConfig().getString("Log-Channel");
        if (jda != null) {
            TextChannel consoleLogChannel = jda.getTextChannelById(DISCORD_CHANNEL);
            if (consoleLogChannel != null) {
                consoleLogChannel.sendMessage(msg).queue();
            }
        }
    }

}

