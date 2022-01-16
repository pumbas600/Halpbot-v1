package nz.pumbas.halpbot.decorators.time;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.utilities.LogLevel;

// Bind the TimeDecorator to this instance so that the factory knows to instantiate this
@ComponentBinding(TimeDecorator.class)
public class TimeDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private final LogLevel logLevel;

    // Its important that you're @Bound constructor contains the ActionInvokable and the annotation so that
    // it can be used by the constructor. If this doesn't match, an exception will be thrown during startup
    @Bound
    public TimeDecorator(ActionInvokable<C> actionInvokable, Time time) {
        super(actionInvokable);
        this.logLevel = time.value();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        OffsetDateTime start = OffsetDateTime.now();
        Exceptional<R> result = super.invoke(invocationContext);

        // Measure the time in milliseconds between now and before the action was invoked to see how long it took
        double ms = start.until(OffsetDateTime.now(), ChronoUnit.NANOS) / 1_000_000D;
        this.logLevel.log(invocationContext.applicationContext(), "Invoked %s %s in %.5fms"
                .formatted(this.executable().qualifiedName(), result.caught() ? "Unsuccessfully" : "Successfully", ms));

        return result;
    }
}
