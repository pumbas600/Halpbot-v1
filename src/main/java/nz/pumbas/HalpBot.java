package nz.pumbas;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.CommandManager;
import nz.pumbas.customparameters.Shape;

public class HalpBot extends ListenerAdapter
{
    private final JDA jda;

    public HalpBot(String token) throws LoginException
    {
        JDABuilder builder = JDABuilder.createDefault(token)
            .addEventListeners(this);

        CommandManager manager = new CommandManager(builder, Shape.class);
        manager.registerCommands(new HalpBotCommands());

        this.jda = builder.build();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        this.jda.getPresence().setPresence(Activity.of(ActivityType.WATCHING, "Trying to halp everyone"), false);
        System.out.printf("The bot is initialised and running in %s servers%n", event.getGuildTotalCount());
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event)
    {
        System.out.println("Shutting down the bot!");
    }
}
