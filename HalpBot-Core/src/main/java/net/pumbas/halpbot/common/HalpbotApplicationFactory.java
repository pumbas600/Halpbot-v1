package net.pumbas.halpbot.common;

import net.pumbas.halpbot.permissions.UsePermissions;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.core.boot.HartshornApplicationFactory;
import org.dockbox.hartshorn.core.context.element.AnnotatedElementModifier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.annotations.UseConfigurations;

import java.lang.annotation.Annotation;

public class HalpbotApplicationFactory extends HartshornApplicationFactory
{
    @Override
    public HartshornApplicationFactory activator(TypeContext<?> activator) {
        Exceptional<Bot> eBot = activator.annotation(Bot.class);
        if (eBot.absent())
            throw new IllegalArgumentException("The bot main class must be annotated with @Bot");

        Bot bot = eBot.get();
        AnnotatedElementModifier.of(activator).add(new Activator()
        {
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
    public HartshornApplicationFactory loadDefaults() {
        return super.loadDefaults()
            .serviceActivator(new UseConfigurations()
            {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return UseConfigurations.class;
                }
            })
            .serviceActivator(new UsePermissions()
            {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return UsePermissions.class;
                }
            });
    }
}
