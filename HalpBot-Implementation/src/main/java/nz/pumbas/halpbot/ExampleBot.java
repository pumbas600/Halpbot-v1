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
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.context.ApplicationContext;

import javax.inject.Inject;

import nz.pumbas.halpbot.common.HalpbotBuilder;
import nz.pumbas.halpbot.common.UseAll;

@UseAll
@Activator
public class ExampleBot extends ListenerAdapter
{
    @Inject private ApplicationContext applicationContext;

    public static void main(String[] args) {
        HalpbotBuilder.create(ExampleBot.class, args)
                .build(token -> JDABuilder.createDefault(token)
                        .setActivity(Activity.of(ActivityType.LISTENING, "to how cool Halpbot is!")));
    }

    @Override
    public void onReady(ReadyEvent event) {
        // When the bot starts up, log the number of guilds it's in
        this.applicationContext.log().info("Bot running in %d guilds".formatted(event.getGuildTotalCount()));
    }
}
