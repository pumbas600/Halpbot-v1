package nz.pumbas.commands.validation;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.BiPredicate;

public class ValidationManager {

    private static final Map<Class<? extends Annotation>, BiPredicate<Annotation, Object>> ValidationMappings = Map.of(
        Min.class, (annotation, object) -> {
                Min min = (Min) annotation;
                Double value = getNumericalValue(object);
                if (null == value) return false;

                return min.inclusive()
                        ? value >= min.value()
                        : value > min.value();
            },
        Max.class, (annotation, object) -> {
                Max max = (Max) annotation;
                Double value = getNumericalValue(object);
                if (null == value) return false;

                return max.isInclusive()
                        ? value <= max.value()
                        : value < max.value();
            }
    );

    /**
     * Retrieves the numerical value of an object.
     * <pre>
     *     For numbers, this is the numerical value.
     *     For
     * </pre>
     * @param object
     * @return
     */
    @Nullable
    private static Double getNumericalValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        else if (object.getClass().isArray()) {
            return (double)Array.getLength(object);
        }
        else if (object instanceof String) {
            return (double)((CharSequence) object).length();
        }
        else return null;
    }

    /**
     * Returns if the {@link Annotation} is a token validator.
     *
     * @param validation
     *      The {@link Annotation} to check
     *
     * @return if the {@link Annotation} is a token validator
     */
    public static boolean isTokenValidator(Class<? extends Annotation> validation) {
        return ValidationMappings.containsKey(validation);
    }

    /**
     * If the passed {@link Object} meets the requirements of the passed in {@link Annotation token validator}.
     * If the {@link Annotation} is not a token validator, then it will return false by default.
     *
     * @param validation
     *      The {@link Annotation token validator}
     * @param object
     *      The {@link Object} to determine if it meets the requirements of the {@link Annotation token validator}
     *
     * @return If the passed {@link Object} meets the requirements of the {@link Annotation token validator}
     */
    public static boolean isValid(Annotation validation, Object object) {
        return ValidationMappings
                .getOrDefault(validation.getClass(), (annotation, obj) -> false)
                .test(validation, object);
    }
}
