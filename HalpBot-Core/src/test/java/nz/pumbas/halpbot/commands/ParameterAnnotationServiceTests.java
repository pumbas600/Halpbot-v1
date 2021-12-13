package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.converters.annotations.Any;
import nz.pumbas.halpbot.converters.annotations.ParameterAnnotation;
import nz.pumbas.halpbot.converters.annotations.parameter.Children;
import nz.pumbas.halpbot.converters.annotations.parameter.Description;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unmodifiable;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

@UseCommands
@HartshornTest
public class ParameterAnnotationServiceTests
{
    @SafeVarargs
    private boolean isValid(ParameterAnnotationService parameterAnnotationService,
                            Class<?> parameterType,
                            Class<? extends Annotation>... parameterAnnotations)
    {
        return parameterAnnotationService.isValid(
                TypeContext.of(parameterType),
                Stream.of(parameterAnnotations).map(TypeContext::of).collect(Collectors.toList())
        );
    }

    @InjectTest
    public void remainingAnnotationAllowedTypesTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                String.class,
                Remaining.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                int.class,
                Remaining.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                Object.class,
                Remaining.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                float.class,
                Remaining.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                User.class,
                Remaining.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                HalpbotCore.class,
                Remaining.class));
    }

    @InjectTest
    public void unrequiredAnnotationAllowedTypesTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                String.class,
                Unrequired.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                int.class,
                Unrequired.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Object.class,
                Unrequired.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                float[].class,
                Unrequired.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                List.class,
                Unrequired.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                HalpbotCore.class,
                Unrequired.class));
    }

    @InjectTest
    public void implicitAnnotationAllowedTypesTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                String[].class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                int[].class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Object[].class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                float[].class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                List.class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Set.class,
                Implicit.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Collection.class,
                Implicit.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                String.class,
                Implicit.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                int.class,
                Implicit.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                Object.class,
                Implicit.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                HalpbotCore.class,
                Implicit.class));
    }

    @InjectTest
    public void sourceAnnotationAllowedTypesAndConflictingAnnotationsTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                String.class,
                Source.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                int.class,
                Source.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Object.class,
                Source.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                User.class,
                Source.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                String.class,
                Source.class, Unrequired.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                User.class,
                Source.class, Unrequired.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                HalpbotCore.class,
                Source.class, Unrequired.class));
    }

    @InjectTest
    public void unmodifableAnnotationAllowedTypesTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                List.class,
                Unmodifiable.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Set.class,
                Unmodifiable.class));
        Assertions.assertTrue(this.isValid(
                parameterAnnotationService,
                Collection.class,
                Unmodifiable.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                User.class,
                Unmodifiable.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                String.class,
                Unmodifiable.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                int.class,
                Unmodifiable.class));
        Assertions.assertFalse(this.isValid(
                parameterAnnotationService,
                HalpbotCore.class,
                Unmodifiable.class));
    }

    @InjectTest
    public void isRegisteredParameterAnnotationTest(ParameterAnnotationService parameterAnnotationService) {
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Children.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Description.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Implicit.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Remaining.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Source.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Unmodifiable.class)));
        Assertions.assertTrue(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Unrequired.class)));
        Assertions.assertFalse(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Any.class)));
        Assertions.assertFalse(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(ParameterAnnotation.class)));
    }

    @InjectTest
    public void sortAndFilterTest(ParameterAnnotationService parameterAnnotationService) {
        Stream<TypeContext<? extends Annotation>> types = Stream.of(Any.class, Unmodifiable.class, Unrequired.class, Implicit.class)
                .map(TypeContext::of);
        List<TypeContext<? extends Annotation>> processed = parameterAnnotationService.sortAndFilter(types);

        Assertions.assertEquals(3, processed.size());
        Assertions.assertEquals(Unmodifiable.class,processed.get(0).type());
        Assertions.assertEquals(Unrequired.class,processed.get(1).type());
        Assertions.assertEquals(Implicit.class,processed.get(2).type());
    }
}
