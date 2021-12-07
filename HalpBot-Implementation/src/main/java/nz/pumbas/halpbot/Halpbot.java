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

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.exceptions.UndefinedActivatorException;
import nz.pumbas.halpbot.common.annotations.Bot;
import nz.pumbas.halpbot.configurations.EmbedStringsDisplayConfiguration;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
@Activator
@UseCommands
@Bot(prefix = "$", displayConfiguration = EmbedStringsDisplayConfiguration.class)
public class Halpbot extends ListenerAdapter
{
    public static final long CREATOR_ID = 260930648330469387L;

    @Inject private ApplicationContext applicationContext;
    @Inject private HalpbotCore halpbotCore;

    public Halpbot() throws LoginException, UndefinedActivatorException, IOException {
        this.create();
    }

    private void create() throws LoginException, UndefinedActivatorException, IOException {
        String token = HalpbotUtils.getFirstLine(new ClassPathResource("static/Token.txt").getInputStream());

        JDABuilder builder = JDABuilder.createDefault(token)
                .disableIntents(GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(this);

        ErrorManager.setLoggerUserId(CREATOR_ID);

        this.halpbotCore.setOwner(CREATOR_ID)
                .build(builder);
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.halpbotCore.jda().getPresence()
                .setPresence(Activity.of(ActivityType.WATCHING, "Trying to halp everyone"), false);
        this.applicationContext.log()
                .info("The bot is initialised and running in %d servers".formatted(event.getGuildTotalCount()));
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        this.applicationContext.log().info("Shutting down the bot!");
    }
}
