package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Binds(ParameterAnnotationContext.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class HalpbotParameterAnnotationContext implements ParameterAnnotationContext
{
    public static final HalpbotParameterAnnotationContext GENERIC = generic();

    @NotNull private final Set<TypeContext<? extends Annotation>> afterAnnotations;

    @NotNull
    @Setter private Set<TypeContext<? extends Annotation>> conflictingAnnotations;

    @NotNull
    @Setter private Set<TypeContext<?>> allowedTypes;

    @Override
    public void addAfterAnnotation(@NotNull TypeContext<? extends Annotation> afterAnnotation) {
        this.afterAnnotations.add(afterAnnotation);
    }

    public static HalpbotParameterAnnotationContext generic() {
        return new HalpbotParameterAnnotationContext(
                HartshornUtils.emptySet(),
                HartshornUtils.emptySet(),
                HartshornUtils.asSet(TypeContext.of(Object.class)));
    }
}
