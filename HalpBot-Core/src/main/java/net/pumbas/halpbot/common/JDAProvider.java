package net.pumbas.halpbot.common;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.pumbas.halpbot.configurations.BotConfiguration;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

import java.util.function.Function;

import javax.security.auth.login.LoginException;

import jakarta.inject.Singleton;

@Service
public class JDAProvider {

    @Provider
    @Singleton
    public JDA jdaProvider(final ApplicationContext applicationContext, final BotConfiguration botConfiguration)
        throws LoginException, InterruptedException
    {
        final Function<String, JDABuilder> jdaBuilder = HalpbotBuilder.instance().jdaBuilder();
        if (jdaBuilder == null) {
            throw new IllegalStateException("JDABuilder has not been set");
        }

        applicationContext.log().info("Constructing JDA instance");

        final JDABuilder builder = jdaBuilder.apply(botConfiguration.token());
        final JDA jda = builder.build();
        jda.awaitReady();

        applicationContext.log().info("JDA instance created");
        return jda;
    }
}
