package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

import java.lang.annotation.Annotation;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
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
    public void remainingAnnotationAllowedTypes(ParameterAnnotationService parameterAnnotationService) {
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
}
