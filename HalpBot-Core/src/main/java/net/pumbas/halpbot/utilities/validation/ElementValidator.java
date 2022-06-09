package net.pumbas.halpbot.utilities.validation;

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
public class ElementValidator {

    private final List<ValidationPredicate> validationPredicates;

    public static ElementValidatorBuilder build(final String handlerName) {
        return new ElementValidatorBuilder(handlerName);
    }

    public static ElementValidator publicModifier(final String handlerName) {
        return new ElementValidatorBuilder(handlerName).modifiers(AccessModifier.PUBLIC).create();
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

    public static class ElementValidatorBuilder {

        private final String handlerName;
        private final List<ValidationPredicate> validationPredicates = new ArrayList<>();

        public ElementValidatorBuilder(final String handlerName) {
            this.handlerName = handlerName;
        }

        public ElementValidatorBuilder modifiers(final AccessModifier... modifiers) {
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

        public ElementValidatorBuilder returnType(final Class<?> returnType) {
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

        public ElementValidatorBuilder parameterCount(final int parameterCount) {
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

        public ElementValidatorBuilder parameter(final int index, final Class<?> type) {
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

        public ElementValidator create() {
            return new ElementValidator(this.validationPredicates);
        }
    }
}
