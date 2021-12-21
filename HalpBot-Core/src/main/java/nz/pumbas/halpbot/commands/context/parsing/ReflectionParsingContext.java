package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

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
                Exceptional<CommandContext> commandContext = commandAdapter.reflectiveCommandContextSafely(targetType, methodName.get());
                if (commandContext.present() && commandContext.get().executable() instanceof MethodContext methodContext
                    && invocationContext.reflections().contains(methodContext.parent()))
                {
                    Exceptional<Object> result = commandContext.get().invoke(invocationContext, true);
                    if (!result.caught()) {
                        return Exceptional.of(() -> {
                            invocationContext.assertNext(')');
                            return result.orNull();
                        });
                    }
                }
            }
        }
        return Exceptional.of(ParsingContext.IGNORE_RESULT);
    }
}
