package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.utilities.HalpbotStringTraverser;
import nz.pumbas.halpbot.utilities.StringTraverser;

@Singleton
@ComponentBinding(TokenService.class)
public class HalpbotTokenService implements TokenService
{
    private final Map<ExecutableElementContext<?, ?>, List<Token>> cache = new ConcurrentHashMap<>();

    @Inject
    @Getter private ApplicationContext applicationContext;

    @Inject private TokenFactory tokenFactory;
    @Inject private CommandAdapter commandAdapter;
    @Inject private ConverterHandler converterHandler;

    @Override
    public List<Token> tokens(ExecutableElementContext<?, ?> executableContext) {
        if (this.cache.containsKey(executableContext))
            return this.cache.get(executableContext);

        List<Token> tokens = new ArrayList<>();
        List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        Exceptional<String> command = this.command(executableContext);
        if (command.present() && !command.get().isBlank())
        {
            StringTraverser stringTraverser = new HalpbotStringTraverser(command.get());

            while (stringTraverser.hasNext()) {
                if (parameterIndex < parameters.size()) {
                    ParameterContext<?> currentParameter = parameters.get(parameterIndex);
                    int currentIndex = stringTraverser.currentIndex();

                    if (!this.converterHandler.isCommandParameter(currentParameter) ||
                       stringTraverser.next().equalsIgnoreCase(this.commandAdapter.typeAlias(currentParameter.type())))
                    {
                        tokens.add(this.tokenFactory.createParsing(currentParameter));
                        parameterIndex++;
                        continue;
                    }

                    stringTraverser.currentIndex(currentIndex);
                }
                PlaceholderToken placeholderToken = stringTraverser.nextSurrounded("[", "]")
                        .map(placeholder -> this.tokenFactory.createPlaceholder(true, placeholder))
                        .orElse(() -> stringTraverser.nextSurrounded("<", ">")
                                .map(placeholder -> this.tokenFactory.createPlaceholder(false, placeholder))
                                .orNull())
                        .orThrowUnchecked(() -> new ApplicationException(
                                "Placeholders must be surrounded by either '[...]' or '<...>' in the command %s"
                                        .formatted(executableContext.qualifiedName())));

                tokens.add(placeholderToken);
            }
            if (parameterIndex < parameters.size())
                while (parameterIndex < parameters.size()) {
                    if (this.converterHandler.isCommandParameter(parameters.get(parameterIndex++)))
                        ExceptionHandler.unchecked(new ApplicationException(
                                "All command parameters must be specified in the command"));
                }

        }
        else tokens.addAll(executableContext.parameters()
                .stream()
                .map(parameterContext -> this.tokenFactory.createParsing(parameterContext))
                .collect(Collectors.toList()));

        this.cache.put(executableContext, tokens);
        return tokens;
    }

    private Exceptional<String> command(ExecutableElementContext<?, ?> executableContext) {
        return executableContext.annotation(Command.class)
                .map(Command::command)
                .orElse(() -> executableContext.annotation(CustomConstructor.class).map(CustomConstructor::command).orNull());

    }
}
