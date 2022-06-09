package net.pumbas.halpbot.utilities.handlervalidation;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.AccessModifier;
import org.dockbox.hartshorn.util.reflect.AnnotatedMemberContext;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HandlerValidator {

    private final List<ValidationPredicate> validationPredicates;

    public static HandlerValidatorBuilder build(final String handlerName) {
        return new HandlerValidatorBuilder(handlerName);
    }

    public boolean isValid(final ApplicationContext context,
                           final AnnotatedMemberContext<?> element)
    {
        for (final ValidationPredicate validationPredicate : this.validationPredicates) {
            if (!validationPredicate.test(context, element))
                return false;
        }
        return true;
    }

    public static class HandlerValidatorBuilder {

        private final String handlerName;
        private final List<ValidationPredicate> validationPredicates = new ArrayList<>();

        public HandlerValidatorBuilder(final String handlerName) {
            this.handlerName = handlerName;
        }

        public HandlerValidatorBuilder modifiers(final AccessModifier... modifiers) {
            for (final AccessModifier modifier : modifiers) {
                this.validationPredicates.add((context, element) -> {
                    if (!element.has(modifier)) {
                        context.log().warn("The %s %s must have the %s modifier"
                            .formatted(this.handlerName, element.qualifiedName(), modifier));
                        return false;
                    }
                    return true;
                });
            }
            return this;
        }

        public HandlerValidatorBuilder returnType(final Class<?> returnType) {
            this.validationPredicates.add((context, element) -> {
                if (element instanceof MethodContext<?, ?> methodContext && methodContext.returnType().is(returnType)) {
                    return true;
                }
                context.log().warn("The %s %s must have the return type %s"
                    .formatted(this.handlerName, element.qualifiedName(), returnType.getCanonicalName()));
                return false;
            });
            return this;
        }

        public HandlerValidatorBuilder parameterCount(final int parameterCount) {
            this.validationPredicates.add((context, element) -> {
                if (!(element instanceof ExecutableElementContext<?, ?> executable) ||
                    executable.parameterCount() != parameterCount) {
                    context.log().warn("The %s %s must have %d parameter%s"
                        .formatted(this.handlerName, element.qualifiedName(), parameterCount, parameterCount == 1 ? "" : "s"));
                    return false;
                }
                return true;
            });
            return this;
        }

        public HandlerValidatorBuilder parameter(final int index, final Class<?> type) {
            this.validationPredicates.add((context, element) -> {
                if (!(element instanceof ExecutableElementContext<?, ?> executable)) {
                    context.log().warn("The %s %s must be an executable element"
                        .formatted(this.handlerName, element.qualifiedName()));
                    return false;
                }

                final int actualIndex = index < 0 ? executable.parameterCount() - index : index;
                if (actualIndex < 0 || actualIndex >= executable.parameterCount())
                    return false;

                if (!executable.parameters().get(actualIndex).type().is(type)) {
                    context.log().warn("The %s %s must have a %s parameter at index %d"
                        .formatted(this.handlerName, executable.qualifiedName(), type.getCanonicalName(), actualIndex));
                    return false;
                }
                return true;
            });
            return this;
        }

        public HandlerValidator create() {
            return new HandlerValidator(this.validationPredicates);
        }
    }
}
