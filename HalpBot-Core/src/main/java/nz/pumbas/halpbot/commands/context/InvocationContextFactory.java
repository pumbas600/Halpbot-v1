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
    CommandInvocationContext create(String content,
                                    @Nullable HalpbotEvent halpbotEvent);

    default CommandInvocationContext create(String content) {
        return this.create(content, null);
    }
}
