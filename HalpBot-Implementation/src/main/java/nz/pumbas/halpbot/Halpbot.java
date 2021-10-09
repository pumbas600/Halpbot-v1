/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.adapters.ButtonAdapter;
import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.adapters.ReactionAdapter;
import nz.pumbas.halpbot.commands.chemmat.ChemmatCommands;
import nz.pumbas.halpbot.commands.NumberSystemConverters;
import nz.pumbas.halpbot.commands.chemmat.QuestionConfirmationCommands;
import nz.pumbas.halpbot.commands.chemmat.QuestionStatisticCommands;
import nz.pumbas.halpbot.commands.commandadapters.SimpleCommandAdapter;
import nz.pumbas.halpbot.commands.ElectricalCommands;
import nz.pumbas.halpbot.commands.HalpBotCommands;
import nz.pumbas.halpbot.commands.KotlinCommands;
import nz.pumbas.halpbot.commands.MatrixCommands;
import nz.pumbas.halpbot.commands.VectorCommands;
import nz.pumbas.halpbot.configurations.EmbedStringsDisplayConfiguration;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class Halpbot extends ListenerAdapter
{
    public static final long CREATOR_ID = 260930648330469387L;
    private final JDA jda;
    private static HalpbotCore halpbotCore;
    private final ApplicationContext context;

    public Halpbot(ApplicationContext context, String token) throws LoginException
    {
        this.context = context;
        JDABuilder builder = JDABuilder.createDefault(token)
            .disableIntents(GatewayIntent.GUILD_PRESENCES)
            .addEventListeners(this);

        ErrorManager.setLoggerUserId(CREATOR_ID);

        halpbotCore = new HalpbotCore(builder)
            .setOwner(CREATOR_ID)
            .addAdapters(
                new SimpleCommandAdapter("$"),
                new ReactionAdapter(),
                new ButtonAdapter())
            .displayConfiguration(new EmbedStringsDisplayConfiguration())
            .register(
                new KotlinCommands(),
                new MatrixCommands(),
                new VectorCommands(),
                new ElectricalCommands(),
                new HalpBotCommands(),
                new NumberSystemConverters(),
                context.getBean(ChemmatCommands.class),
                context.getBean(QuestionConfirmationCommands.class),
                context.getBean(QuestionStatisticCommands.class))
            .registerAdapters();

        this.jda = halpbotCore.build();
    }

    public static ReactionAdapter getReactionAdapter() {
        return halpbotCore.get(ReactionAdapter.class);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        this.jda.getPresence().setPresence(Activity.of(ActivityType.WATCHING, "Trying to halp everyone"), false);
        HalpbotUtils.logger().info("The bot is initialised and running in {} servers", event.getGuildTotalCount());
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event)
    {
        HalpbotUtils.logger().info("Shutting down the bot!");
        ((ConfigurableApplicationContext) this.context).close();
    }
}
