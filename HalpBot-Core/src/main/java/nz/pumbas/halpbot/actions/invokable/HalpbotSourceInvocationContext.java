package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Getter
@Accessors(chain = false)
@Binds(SourceInvocationContext.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public class HalpbotSourceInvocationContext implements SourceInvocationContext
{
    @Inject private ApplicationContext applicationContext;
    @Setter private TypeContext<?> currentType;

    private final HalpbotEvent halpbotEvent;
    private final List<ParsingToken> nonCommandParameterTokens;
}
