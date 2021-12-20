package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;

@Service
public interface CommandContextFactory
{
    @Factory
    CommandContext create(List<String> aliases,
                          String description,
                          String usage,
                          @Nullable Object instance,
                          ExecutableElementContext<?> executable,
                          List<String> permissions,
                          Set<TypeContext<?>> reflections,
                          ParsingContext parsingContext);
}
