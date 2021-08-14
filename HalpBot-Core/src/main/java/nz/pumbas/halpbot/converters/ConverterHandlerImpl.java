package nz.pumbas.halpbot.converters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import nz.pumbas.halpbot.objects.Tuple;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.context.LateInit;

public class ConverterHandlerImpl implements ConverterHandler, LateInit
{

    private final Map<Class<?>, List<ConverterContext>> converters = new HashMap<>();
    private final List<Tuple<Predicate<Class<?>>, ConverterContext>> fallbackConverters = new ArrayList<>();

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        // Load the parsers
        Converters.load();
    }

    /**
     * Retrieves the {@link Converter} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *      The {@link Class type} of the {@link TypeConverter}
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Converter<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx) {
        if (this.converters.containsKey(type)) {
            return (Converter<T>) this.retrieveConverterByAnnotation(this.converters.get(type), ctx);
        }
        return (Converter<T>) this.converters.keySet()
            .stream()
            .filter(c -> c.isAssignableFrom(type))
            .findFirst()
            .map(c -> (Object) this.retrieveConverterByAnnotation(this.converters.get(c), ctx))
            .orElseGet(() -> this.fallbackConverters.stream()
                .filter(t -> this.filterFallbackConverters(t, type, ctx))
                .findFirst()
                .map(t -> t.getValue().getConverter())
                .orElse(Reflect.cast(Converters.OBJECT_CONVERTER)));
    }

    /**
     * Retrieves the {@link Converter} with the corresponding annotation from the list of {@link ConverterContext}.
     *
     * @param converterContexts
     *      The list of {@link ConverterContext}
     * @param ctx
     *      The {@link MethodContext}
     *
     * @return The {@link Converter} with the corresponding annotation
     */
    private Converter<?> retrieveConverterByAnnotation(@NotNull List<ConverterContext> converterContexts, @NotNull MethodContext ctx) {
        if (converterContexts.isEmpty())
            return Converters.OBJECT_CONVERTER;

        for (ConverterContext converterContext : converterContexts) {
            if (ctx.getContextState().getAnnotationTypes().contains(converterContext.getAnnotationType())) {
                ctx.getContextState().getAnnotationTypes().remove(converterContext.getAnnotationType());
                return converterContext.getConverter();
            }
        }
        // Otherwise return the last converter
        return converterContexts.get(converterContexts.size() - 1).getConverter();
    }

    private boolean filterFallbackConverters(@NotNull Tuple<Predicate<Class<?>>, ConverterContext> tuple,
                                             @NotNull Class<?> type, @NotNull MethodContext ctx) {
        if (!tuple.getKey().test(type)) return false;

        Class<?> annotationType = tuple.getValue().getAnnotationType();
        if (annotationType.isAssignableFrom(Void.class))
            return true;
        else if (ctx.getContextState().getAnnotationTypes().contains(annotationType)) {
            ctx.getContextState().getAnnotationTypes().remove(annotationType);
            return true;
        }
        return false;
    }

    /**
     * Registers a {@link Converter} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *      The type of the {@link TypeConverter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    @Override
    public void registerConverter(@NotNull Class<?> type, @NotNull Class<?> annotationType, @NotNull Converter<?> converter) {
        if (!this.converters.containsKey(type))
            this.converters.put(type, new ArrayList<>());

        List<ConverterContext> converterContexts = this.converters.get(type);
        for (int i = 0; i < converterContexts.size(); i++) {
            if (0 < converterContexts.get(i).getConverter().getPriority().compareTo(converter.getPriority())) {
                converterContexts.add(i, new ConverterContext(annotationType, converter));
                return;
            }
        }
        converterContexts.add(new ConverterContext(annotationType, converter));
    }

    /**
     * Registers a {@link Converter} against the {@link Predicate filter} with the specified {@link Class annotation type}.
     *
     * @param filter
     *      The {@link Predicate filter} for this {@link Converter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    @Override
    public void registerConverter(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                                  @NotNull Converter<?> converter) {

        Tuple<Predicate<Class<?>>, ConverterContext> converterContext =
            Tuple.of(filter, new ConverterContext(annotationType, converter));

        for (int i = 0; i < this.fallbackConverters.size(); i++) {
            if (0 < this.fallbackConverters.get(i).getValue().getConverter().getPriority().compareTo(converter.getPriority())) {
                this.fallbackConverters.add(i, converterContext);
                return;
            }
        }
        this.fallbackConverters.add(converterContext);
    }
}