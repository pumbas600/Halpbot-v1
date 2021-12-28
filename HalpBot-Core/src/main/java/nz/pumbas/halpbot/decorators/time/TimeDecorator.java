package nz.pumbas.halpbot.decorators.time;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.utilities.LogLevel;

@Binds(TimeDecorator.class)
public class TimeDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    @Getter private final LogLevel logLevel;

    @Bound
    public TimeDecorator(ActionInvokable<C> actionInvokable, Time time) {
        super(actionInvokable);
        this.logLevel = time.value();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        OffsetDateTime start = OffsetDateTime.now();
        Exceptional<R> result = super.invoke(invocationContext);

        double ms = start.until(OffsetDateTime.now(), ChronoUnit.NANOS) / 1000D;
        this.logLevel.log(invocationContext.applicationContext(),
                "Invoked [%s] in %.2fms".formatted(this.executable().qualifiedName(), ms));

        return result;
    }
}
