package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.core.boot.HartshornApplicationFactory;
import org.dockbox.hartshorn.core.context.element.AnnotatedElementModifier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;

public class HalpbotApplicationFactory extends HartshornApplicationFactory
{
    @Override
    public HartshornApplicationFactory activator(TypeContext<?> activator) {
        if (activator.annotation(Activator.class).absent()) {
            Exceptional<Bot> eBot = activator.annotation(Bot.class);
            if (eBot.absent())
                throw new IllegalArgumentException(
                        "The bot main class must be annotated with either @Activator or @Bot");

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
        }
        return super.activator(activator);
    }
}
