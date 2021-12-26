package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Collection;

import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public interface ReflectionConverter
{
    default Exceptional<Object> parseReflection(CommandInvocationContext invocationContext)
    {
        if (!invocationContext.reflections().isEmpty()) {
            TypeContext<?> targetType = invocationContext.currentType();

            Exceptional<String> methodName = invocationContext.next("(");
            if (methodName.present()) {
                CommandAdapter commandAdapter = invocationContext.applicationContext().get(CommandAdapter.class);
                Collection<CommandContext> commandContexts = commandAdapter.reflectiveCommandContext(
                        targetType, methodName.get(), invocationContext.reflections());
                if (!commandContexts.isEmpty())
                {
                    int currentIndex = invocationContext.currentIndex();
                    invocationContext.canHaveContextLeft(true);
                    for (CommandContext commandContext : commandContexts) {
                        Exceptional<Object> result = commandContext.invoke(invocationContext);
                        if (!result.caught() && invocationContext.isNext(')'))
                            return result;
                        else invocationContext.currentIndex(currentIndex);
                    }
                }
            }
        }
        return Exceptional.of(HalpbotUtils.IGNORE_RESULT);
    }
}
