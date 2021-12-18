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

import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.io.IOException;

import javax.inject.Inject;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.common.Bot;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
@Activator
@UseCommands
public class Halpbot extends ListenerAdapter implements Bot
{
    public static final long CREATOR_ID = 260930648330469387L;

    @Inject private ApplicationContext applicationContext;
    @Inject private HalpbotCore halpbotCore;

    @Override
    public void onReady(ReadyEvent event) {
        this.applicationContext.log()
                .info("The bot is initialised and running in %d servers".formatted(event.getGuildTotalCount()));
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        this.applicationContext.log().info("Shutting down the bot!");
    }

    @Override
    public JDABuilder initialise() {
        String token = HalpbotUtils.getFirstLine(HalpbotUtils.retrieveReader("Token.txt").get());
        return JDABuilder.createDefault(token)
                .disableIntents(GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(this)
                .setActivity(Activity.of(ActivityType.COMPETING, "The best way to help everyone"));
    }
}
