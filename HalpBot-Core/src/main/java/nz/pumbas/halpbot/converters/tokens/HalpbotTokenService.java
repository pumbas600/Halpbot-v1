package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;

@Service
@Binds(TokenService.class)
public class HalpbotTokenService implements TokenService
{
    private final Map<ExecutableElementContext<?>, List<Token>> cache = HartshornUtils.emptyMap();

    @Inject
    @Getter private ApplicationContext applicationContext;

    @Inject private TokenFactory tokenFactory;

    //TODO: Support placeholder tokens
    @Override
    public List<Token> tokens(ExecutableElementContext<?> executableContext) {
        if (this.cache.containsKey(executableContext))
            return this.cache.get(executableContext);

        List<Token> tokens = executableContext.parameters()
                .stream()
                .map(parameterContext -> this.tokenFactory.createParsing(parameterContext))
                .collect(Collectors.toList());

        this.cache.put(executableContext, tokens);
        return tokens;
    }
}
