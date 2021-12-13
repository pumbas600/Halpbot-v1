package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import nz.pumbas.halpbot.events.HalpbotEvent;

@Service
public interface InvocationContextFactory
{
    @Factory
    InvocationContext create(String content,
                             @Nullable HalpbotEvent halpbotEvent,
                             Set<TypeContext<?>> reflections);

    default InvocationContext create(String content) {
        return this.create(content, null, Collections.emptySet());
    }
}
