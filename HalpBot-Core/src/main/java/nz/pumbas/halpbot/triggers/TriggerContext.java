package nz.pumbas.halpbot.triggers;

import java.util.List;
import java.util.stream.Stream;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;
import nz.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import nz.pumbas.halpbot.permissions.Require;

public interface TriggerContext extends SourceContext<SourceInvocationContext>, DisplayableResult
{
    List<String> triggers();

    String description();

    TriggerStrategy strategy();

    Require require();

    default boolean matches(String message) {
        Stream<String> stream = this.triggers().stream();

        return switch (this.require()) {
            case ALL -> stream.allMatch(trigger -> this.strategy().contains(message, trigger));
            case ANY -> stream.anyMatch(trigger -> this.strategy().contains(message, trigger));
        };
    }
}
