package net.pumbas.halpbot.common;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.pumbas.halpbot.configurations.BotConfiguration;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

import java.util.function.Function;

import javax.security.auth.login.LoginException;

@Service
public class JDAProvider {

    @Nullable
    private JDA jdaInstance;

    @Provider
    public JDA jda(final ApplicationContext applicationContext, final BotConfiguration botConfiguration)
        throws LoginException, InterruptedException
    {
        if (this.jdaInstance == null) {
            final Function<String, JDABuilder> jdaBuilder = HalpbotBuilder.instance().jdaBuilder();
            if (jdaBuilder == null) {
                throw new IllegalStateException("JDABuilder has not been set");
            }

            applicationContext.log().info("Constructing JDA instance");

            final JDABuilder builder = jdaBuilder.apply(botConfiguration.token());
            this.jdaInstance = builder.build();
            this.jdaInstance.awaitReady();

            applicationContext.log().info("JDA instance created");
        }
        return this.jdaInstance;
    }
}
