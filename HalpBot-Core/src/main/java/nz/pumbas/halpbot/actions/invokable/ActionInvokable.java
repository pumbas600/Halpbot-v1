package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.function.CheckedFunction;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface ActionInvokable<P extends ActionParsingContext<C>, C extends ActionContext> extends Invokable
{
    P parsingContext();

    default <R> Exceptional<R> invoke(C actionContext) {
        return this.parsingContext()
                .parameters(actionContext,this)
                .flatMap((CheckedFunction<Object[], Exceptional<R>>) this::invoke);
    }
}
