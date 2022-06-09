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

    /**
     * Constructs a new {@link ElementValidatorBuilder} instance with the specified handler name that can be used to
     * construct an element validator. The handler name is used to create helpful warnings when an element fails one of
     * the validation checks.
     *
     * @param handlerName
     *     The name of the handler
     *
     * @return A new {@link ElementValidatorBuilder} instance
     */
    public static ElementValidatorBuilder build(final String handlerName) {
        return new ElementValidatorBuilder(handlerName);
    }

    /**
     * Constructs a new {@link ElementValidator} instance which validates that the element has the public modifier. The
     * handler name is used to create helpful warnings when an element fails one of the validation checks.
     *
     * @param handlerName
     *     The name of the handler
     *
     * @return A new {@link ElementValidator} instance
     */
    public static ElementValidator publicModifier(final String handlerName) {
        return new ElementValidatorBuilder(handlerName).modifiers(AccessModifier.PUBLIC).create();
    }

    /**
     * Checks if the given element is valid by checking all the validators. The validators are instantly stopped once
     * one of them returns false.
     *
     * @param context
     *     The application context. This is used for logging warnings when the member fails a validation check
     * @param element
     *     The element to validate
     *
     * @return True if the element passes the validation criteria, false otherwise
     */
    public boolean isValid(final ApplicationContext context, final AnnotatedMemberContext<?> element) {
        for (final ValidationPredicate validationPredicate : this.validationPredicates) {
            if (!validationPredicate.test(context, element))
                return false;
        }
        return true;
    }

    public static class ElementValidatorBuilder {

        private final String handlerName;
        private final List<ValidationPredicate> validationPredicates = new ArrayList<>();

        /**
         * Creates a new {@link ElementValidatorBuilder} with the given handler name. The handler name is used to create
         * helpful warnings when an element fails one of the validation checks.
         *
         * @param handlerName
         *     The name of the handler
         */
        public ElementValidatorBuilder(final String handlerName) {
            this.handlerName = handlerName;
        }

        /**
         * Validates that the element has all the modifiers specified.
         *
         * @param modifiers
         *     The modifiers the element must have
         *
         * @return Itself for chaining
         */
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

        /**
         * Validates that the element is an {@link MethodContext} and has the specified return type.
         *
         * @param returnType
         *     The return type the method should have
         *
         * @return Itself for chaining
         */
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

        /**
         * Validates that the {@link ExecutableElementContext} has the specified number of parameters.
         *
         * @param parameterCount
         *     The number of parameters the executable element must have
         *
         * @return Itself for chaining
         */
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

        /**
         * Validates that the {@link ExecutableElementContext} has the specified parameter type at the index. If the
         * index is negative, it wraps around to the end of the parameter list. I.e. -1 is the last parameter.
         *
         * @param index
         *     The index of the parameter to validate
         * @param type
         *     The type of the parameter
         *
         * @return Itself for chaining
         */
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

        /**
         * Constructs the {@link ElementValidator} which can be used to validate elements.
         *
         * @return The constructed {@link ElementValidator}
         */
        public ElementValidator create() {
            return new ElementValidator(this.validationPredicates);
        }
    }
}
