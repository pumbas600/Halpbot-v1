package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.InvocationContextFactory;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

@Service
@Binds(TokenService.class)
public class HalpbotTokenService implements TokenService
{
    private final Map<ExecutableElementContext<?>, List<Token>> cache = HartshornUtils.emptyMap();

    @Inject
    @Getter private ApplicationContext applicationContext;

    @Inject private TokenFactory tokenFactory;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private ConverterHandler converterHandler;

    //TODO: Support placeholder tokens
    @Override
    public List<Token> tokens(ExecutableElementContext<?> executableContext) {
        if (this.cache.containsKey(executableContext))
            return this.cache.get(executableContext);

        List<Token> tokens = new ArrayList<>();
        List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        if (executableContext.annotation(Command.class).present() &&
            !executableContext.annotation(Command.class).map(Command::command).or("").isBlank())
        {
            String command = executableContext.annotation(Command.class).map(Command::command).get();
            //TODO: String utils to separate interface
            InvocationContext invocationContext = this.invocationContextFactory.create(command);

            while (invocationContext.hasNext()) {
                ParameterContext<?> currentParameter = parameters.get(parameterIndex);

                if (!this.converterHandler.isCommandParameter(currentParameter)) {
                    tokens.add(this.tokenFactory.createParsing(currentParameter));
                    parameterIndex++;
                    continue;
                }

                String next = invocationContext.next();
                if (next.equalsIgnoreCase(currentParameter.type().name())) {
                    tokens.add(this.tokenFactory.createParsing(currentParameter));
                    parameterIndex++;
                    continue;
                }

                if (!next.startsWith("[") && !next.startsWith("<"))
                    ExceptionHandler.unchecked(new ApplicationException(
                            "The placeholders in the %s command must start with either '<' or '['"
                                    .formatted(executableContext.qualifiedName())));

                boolean isOptional = next.startsWith("[");
                Exceptional<String> ending = invocationContext.next(isOptional ? "]" : ">");
                if (ending.absent())
                    ExceptionHandler.unchecked(new ApplicationException(
                            "The placeholders in the %s command must end with either '>' or ']'"
                                    .formatted(executableContext.qualifiedName())));

                String placeholder = next + " " +  ending.get();
                placeholder = placeholder.substring(1);
                tokens.add(this.tokenFactory.createPlaceholder(isOptional, placeholder));
            }
        }
        else tokens.addAll(executableContext.parameters()
                .stream()
                .map(parameterContext -> this.tokenFactory.createParsing(parameterContext))
                .collect(Collectors.toList()));

        this.cache.put(executableContext, tokens);
        return tokens;
    }

    private String next(int commandIndex, String command) {
        return command.substring(commandIndex, command.indexOf(' ', commandIndex));


    }
}
