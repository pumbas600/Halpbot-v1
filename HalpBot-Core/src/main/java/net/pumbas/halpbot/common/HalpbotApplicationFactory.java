package net.pumbas.halpbot.common;

import net.pumbas.halpbot.permissions.UsePermissions;

import org.dockbox.hartshorn.application.Activator;
import org.dockbox.hartshorn.application.StandardApplicationFactory;
import org.dockbox.hartshorn.data.annotations.UseConfigurations;
import org.dockbox.hartshorn.inject.binding.InjectConfig;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.AnnotatedElementModifier;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.annotation.Annotation;

public class HalpbotApplicationFactory extends StandardApplicationFactory {

    @Override
    public StandardApplicationFactory activator(final TypeContext<?> activator) {
        final Result<Bot> eBot = activator.annotation(Bot.class);
        if (eBot.absent())
            throw new IllegalArgumentException("The bot main class must be annotated with @Bot");

        final Bot bot = eBot.get();
        AnnotatedElementModifier.of(activator).add(new Activator() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Activator.class;
            }

            @Override
            public boolean includeBasePackage() {
                return bot.includeBasePackage();
            }

            @Override
            public String[] scanPackages() {
                return bot.scanPackages();
            }

            @Override
            public InjectConfig[] configs() {
                return bot.configs();
            }
        });
        return super.activator(activator);
    }

    @Override
    public StandardApplicationFactory loadDefaults() {
        return super.loadDefaults()
            .serviceActivator(new UseConfigurations() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return UseConfigurations.class;
                }
            })
            .serviceActivator(new UsePermissions() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return UsePermissions.class;
                }
            });
    }
}
