package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Collection;

import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;

public interface ReflectionParsingContext
{
    default Exceptional<Object> parseReflection(InvocationContext invocationContext)
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
                    for (CommandContext commandContext : commandContexts) {
                        Exceptional<Object> result = commandContext.invoke(invocationContext, true);
                        if (!result.caught() && invocationContext.isNext(')'))
                            return result;
                        else invocationContext.currentIndex(currentIndex);
                    }
                }
            }
        }
        return Exceptional.of(ParsingContext.IGNORE_RESULT);
    }
}
