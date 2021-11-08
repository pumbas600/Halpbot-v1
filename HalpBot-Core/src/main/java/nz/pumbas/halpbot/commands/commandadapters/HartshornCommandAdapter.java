package nz.pumbas.halpbot.commands.commandadapters;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.boot.ApplicationState.Started;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.events.EngineChangedState;
import org.dockbox.hartshorn.events.annotations.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;
import nz.pumbas.halpbot.commands.exceptions.IllegalPrefixException;
import nz.pumbas.halpbot.commands.exceptions.UndefinedActivatorException;
import nz.pumbas.halpbot.common.annotations.Bot;

@Service
@Binds(CommandAdapter.class)
public class HartshornCommandAdapter extends HalpbotAdapter implements CommandAdapter
{
    @Getter
    private String prefix;

    @Inject
    private ApplicationContext context;
    

    @Listener
    public void on(EngineChangedState<Started> event) throws UndefinedActivatorException, IllegalPrefixException {
        if (!this.context.hasActivator(Bot.class))
            throw new UndefinedActivatorException("The @Bot activator must be present on the main class");

        this.prefix = this.context.activator(Bot.class).prefix();
        if (this.prefix.isBlank())
            throw new IllegalPrefixException("The prefix defined in @Bot cannot be blank");
    }

    @Override
    public <T> void registerCommands(@NotNull TypeContext<T> typeContext)     {
        T instance = this.context.get(typeContext);
        this.registerCommandMethods(
            instance,
            typeContext,
            typeContext.methods(Command.class));
    }

    @Override
    public CommandMethod commandMethod(String alias) {
        return null;
    }

    private <T> void registerCommandMethods(Object instance,
                                            TypeContext<T> instanceType,
                                            List<MethodContext<?, T>> annotatedMethods)
    {
        for (MethodContext<?, T> methodContext : annotatedMethods) {
            if (!methodContext.isPublic()) {
                this.context.log().warn("The command method %s should be public if its annotated with @Command"
                        .formatted(methodContext.qualifiedName()));
                continue;
            }
            Command command = methodContext.annotation(Command.class).get();

        }
    }

    private List<String> aliases(Command command, MethodContext<?, ?> methodContext) {
        List<String> aliases = HartshornUtils.asList(command.alias())
            .stream()
            .map(alias -> alias.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

        // If an alias hasn't been specified, use the method name
        if (aliases.isEmpty())
            aliases.add(methodContext.name().toLowerCase(Locale.ROOT));
        return aliases;
    }


}
