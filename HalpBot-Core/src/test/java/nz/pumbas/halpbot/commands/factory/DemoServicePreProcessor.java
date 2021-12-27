package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

@AutomaticActivation
public class DemoServicePreProcessor implements ServicePreProcessor<Demo>
{
    private final TypeContext<DemoTests> target = TypeContext.of(DemoTests.class);

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        // Only process the test class
        return type.equals(this.target);
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        context.log().info("Processing %s".formatted(type.qualifiedName()));
    }

    @Override
    public Class<Demo> activator() {
        return Demo.class;
    }
}
