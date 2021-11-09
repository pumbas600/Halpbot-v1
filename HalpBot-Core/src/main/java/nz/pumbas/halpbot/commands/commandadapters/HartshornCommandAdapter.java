package nz.pumbas.halpbot.commands.commandadapters;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.boot.ApplicationState.Started;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.events.EngineChangedState;
import org.dockbox.hartshorn.events.annotations.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;
import nz.pumbas.halpbot.commands.commandmethods.HalpbotCommandContext;
import nz.pumbas.halpbot.commands.exceptions.IllegalPrefixException;
import nz.pumbas.halpbot.commands.exceptions.UndefinedActivatorException;
import nz.pumbas.halpbot.commands.usage.NameVariableBuilder;
import nz.pumbas.halpbot.commands.usage.TypeUsageBuilder;
import nz.pumbas.halpbot.commands.usage.UsageBuilder;
import nz.pumbas.halpbot.common.annotations.Bot;

@Getter
@Service
@Binds(CommandAdapter.class)
public class HartshornCommandAdapter extends HalpbotAdapter implements CommandAdapter, ContextCarrier
{
    private final Map<String, CommandContext> registeredCommands = HartshornUtils.emptyMap();

    private String prefix;
    private UsageBuilder usageBuilder;

    @Inject
    private ApplicationContext applicationContext;


    @Listener
    public void on(EngineChangedState<Started> event) throws UndefinedActivatorException, IllegalPrefixException {
        if (!this.applicationContext.hasActivator(Bot.class))
            throw new UndefinedActivatorException("The @Bot activator must be present on the main class");

        this.prefix = this.applicationContext.activator(Bot.class).prefix();
        if (this.prefix.isBlank())
            throw new IllegalPrefixException("The prefix defined in @Bot cannot be blank");

        this.determineUsageBuilder();
    }

    private void hasMethodParameterNamesVerifier(String sampleParameter) { }

    private void determineUsageBuilder() {
        if ("arg0".equals(TypeContext.of(this)
                .method("hasMethodParameterNamesVerifier")
                .get()
                .parameters()
                .get(0)
                .name()))
        {
            this.usageBuilder = new TypeUsageBuilder();
        } else {
            this.usageBuilder = new NameVariableBuilder();
        }
    }

    @Override
    public <T> void registerCommands(@NotNull TypeContext<T> typeContext)     {
        T instance = this.applicationContext.get(typeContext);
        this.registerCommandContext(
            instance,
            typeContext,
            typeContext.methods(Command.class));
    }

    @Override
    @Nullable
    public CommandContext commandContext(String alias) {
        return this.registeredCommands.get(alias);
    }

    private <T> void registerCommandContext(Object instance,
                                            TypeContext<T> instanceType,
                                            List<MethodContext<?, T>> annotatedMethods)
    {
        for (MethodContext<?, T> methodContext : annotatedMethods) {
            if (!methodContext.isPublic()) {
                this.applicationContext.log().warn("The command method %s should be public if its annotated with @Command"
                        .formatted(methodContext.qualifiedName()));
                continue;
            }
            Command command = methodContext.annotation(Command.class).get();
            List<String> aliases = this.aliases(command, methodContext);
            //TODO: Finish registering commands

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

    private String usage(Command command, MethodContext<?, ?> methodContext) {
        if (!command.usage().isBlank())
            return command.usage();
        else return this.usageBuilder.buildUsage(this.applicationContext, methodContext);
    }

    private <T> CommandContext createCommand(List<String> aliases,
                                             T instance,
                                             Command command,
                                             MethodContext<?, T> methodContext)
    {
        return new HalpbotCommandContext(
            aliases,
            command.description(),
            this.usage(command, methodContext),
            instance,
            methodContext,
            List.of(command.permissions()),
            Stream.of(command.reflections()).map(TypeContext::of).collect(Collectors.toSet()),
            Invokable.tokens(this.applicationContext, methodContext));
    }
}
