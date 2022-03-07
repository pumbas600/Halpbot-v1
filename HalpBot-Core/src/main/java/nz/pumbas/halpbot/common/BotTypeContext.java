package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.core.annotations.inject.InjectConfig;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Map;

public class BotTypeContext<A> extends TypeContext<A>
{
    protected BotTypeContext(Class<A> type) {
        super(type);
        this.initialiseDefaultAnnotations();
    }

    private void initialiseDefaultAnnotations() {
        Map<Class<?>, Annotation> annotations = super.validate();
        if (!annotations.containsKey(Bot.class))
            throw new IllegalArgumentException("A BotTypeContext can only be created for a type annotated with @Bot");

        Bot bot = (Bot) annotations.get(Bot.class);
        annotations.put(Activator.class, new Activator()
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

    public static <A> TypeContext<A> wrap(TypeContext<A> type) {
        return new BotTypeContext<>(type.type());
    }
}
