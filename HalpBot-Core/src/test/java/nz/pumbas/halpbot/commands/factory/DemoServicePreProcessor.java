package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

@AutomaticActivation
public class DemoServicePreProcessor implements ServicePreProcessor<Demo>
{
    private final TypeContext<DemoTests> target = TypeContext.of(DemoTests.class);

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        // Only process the test class
        return key.type().equals(this.target);
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.log().info("Processing %s".formatted(key.type().qualifiedName()));
    }

    @Override
    public Class<Demo> activator() {
        return Demo.class;
    }
}
