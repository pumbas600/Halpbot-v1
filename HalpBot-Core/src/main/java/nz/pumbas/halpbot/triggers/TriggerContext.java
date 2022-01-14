package nz.pumbas.halpbot.triggers;

import java.util.List;
import java.util.stream.Stream;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;
import nz.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import nz.pumbas.halpbot.permissions.Merger;

public interface TriggerContext extends SourceContext<SourceInvocationContext>, DisplayableResult
{
    List<String> triggers();

    String description();

    TriggerStrategy strategy();

    Merger merger();

    default boolean matches(String message) {
        Stream<String> stream = this.triggers().stream();

        return switch (this.merger()) {
            case AND -> stream.allMatch(trigger -> this.strategy().contains(message, trigger));
            case OR  -> stream.anyMatch(trigger -> this.strategy().contains(message, trigger));
        };
    }
}
