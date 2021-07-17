package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.commandadapters.TokenCommandAdapter;
import nz.pumbas.halpbot.commands.CalculusCommands;
import nz.pumbas.halpbot.commands.HalpBotCommands;
import nz.pumbas.halpbot.commands.KotlinCommands;
import nz.pumbas.halpbot.commands.MatrixCommands;
import nz.pumbas.halpbot.commands.SteamLookupCommands;
import nz.pumbas.halpbot.commands.TokenCommands;
import nz.pumbas.halpbot.commands.VectorCommands;
import nz.pumbas.halpbot.friction.FrictionCommands;
import nz.pumbas.processes.PipeProcess;

public class HalpBot extends ListenerAdapter
{
    public static final long CREATOR_ID = 260930648330469387L;

    private final JDA jda;
//    private final PipeProcess process;

    public HalpBot(String token) throws LoginException
    {
        JDABuilder builder = JDABuilder.createDefault(token)
            .addEventListeners(this);

//        this.process = new PipeProcess("python", "PythonProcess.py");

//        CommandManager manager = new CommandManager(builder);
//        manager.registerCommands(
//            new HalpBotCommands(),
//            new SteamLookupCommands(),
//            new FrictionCommands(),
//            new CalculusCommands(this.process)
//        );
        AbstractCommandAdapter commandAdapter = new TokenCommandAdapter(builder, "$")
            .registerCommands(
                new TokenCommands(),
                new KotlinCommands(),
                new MatrixCommands(),
                new VectorCommands());

        this.jda = builder.build();
        commandAdapter.registerSlashCommands(this.jda);
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
//        this.process.closeProcess();
        System.out.println("Shutting down the bot!");
    }
}
