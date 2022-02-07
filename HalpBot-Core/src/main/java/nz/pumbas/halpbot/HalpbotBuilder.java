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

import org.dockbox.hartshorn.core.Modifiers;
import org.dockbox.hartshorn.core.boot.HartshornApplication;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.util.function.Consumer;
import java.util.function.Function;

import nz.pumbas.halpbot.common.Bot;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.utilities.ErrorManager;

public class HalpbotBuilder
{
    private final ApplicationContext applicationContext;
    private final JDABuilder builder;
    private final Bot bot;

    public HalpbotBuilder(ApplicationContext applicationContext, JDABuilder builder, Bot bot)
    {
        this.applicationContext = applicationContext;
        this.builder = builder;
        this.bot = bot;
    }

    private static HalpbotBuilder create(Class<? extends Bot> main,
                                         String[] args,
                                         Modifiers[] modifiers,
                                         Function<String, JDABuilder> jdaBuilder)
    {
        ApplicationContext applicationContext = HartshornApplication.create(main, args, modifiers);
        applicationContext.get(ErrorManager.class); // Create an instance of the ErrorManager
        BotConfiguration botConfiguration = applicationContext.get(BotConfiguration.class);

        return new HalpbotBuilder(
                applicationContext, jdaBuilder.apply(botConfiguration.token()), applicationContext.get(main));
    }

    public static HalpbotBuilder createDefault(Class<? extends Bot> main, String[] args, Modifiers... modifiers) {
        return create(main, args, modifiers, JDABuilder::createDefault);
    }

    public static HalpbotBuilder createLight(Class<? extends Bot> main, String[] args, Modifiers... modifiers) {
        return create(main, args, modifiers, JDABuilder::createLight);
    }

    public static HalpbotBuilder create(Class<? extends Bot> main,
                                        String[] args,
                                        Function<String, JDABuilder> jdaBuilder,
                                        Modifiers... modifiers)
    {
        return create(main, args, modifiers, jdaBuilder);
    }

    public HalpbotBuilder apply(Consumer<JDABuilder> jdaBuilderConsumer) {
        jdaBuilderConsumer.accept(this.builder);
        return this;
    }

    public ApplicationContext build() throws ApplicationException {
        HalpbotCore halpbotCore = this.applicationContext.get(HalpbotCore.class);

        halpbotCore.build(this.builder);
        this.bot.onCreation(this.applicationContext, halpbotCore);
        return this.applicationContext;
    }


    public static ApplicationContext create(Class<? extends Bot> bot, String[] args, Modifiers... modifiers)
            throws ApplicationException
    {
        ApplicationContext applicationContext = HartshornApplication.create(bot, args, modifiers);
        applicationContext.get(ErrorManager.class); // Create an instance of the ErrorManager
        HalpbotCore halpbotCore = applicationContext.get(HalpbotCore.class);
        Bot botInstance = applicationContext.get(bot);

        JDABuilder builder = botInstance.initialise(args);
        halpbotCore.build(builder);
        botInstance.onCreation(applicationContext, halpbotCore);

        return applicationContext;
    }
}
