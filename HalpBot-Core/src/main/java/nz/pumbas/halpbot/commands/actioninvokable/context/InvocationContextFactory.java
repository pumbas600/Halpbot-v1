package nz.pumbas.halpbot.commands.actioninvokable.context;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

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
