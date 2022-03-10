/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.pumbas.halpbot.HalpbotCore;
import net.pumbas.halpbot.commands.annotations.UseCommands;
import net.pumbas.halpbot.converters.annotations.Any;
import net.pumbas.halpbot.converters.annotations.ParameterAnnotation;
import net.pumbas.halpbot.converters.annotations.parameter.Children;
import net.pumbas.halpbot.converters.annotations.parameter.Description;
import net.pumbas.halpbot.converters.annotations.parameter.Implicit;
import net.pumbas.halpbot.converters.annotations.parameter.Remaining;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unmodifiable;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

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
        Assertions.assertFalse(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Unrequired.class)));
        Assertions.assertFalse(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(Any.class)));
        Assertions.assertFalse(parameterAnnotationService.isRegisteredParameterAnnotation(TypeContext.of(ParameterAnnotation.class)));
    }

    @InjectTest
    public void sortAndFilterTest(ParameterAnnotationService parameterAnnotationService) {
        Stream<TypeContext<? extends Annotation>> types = Stream.of(Unrequired.class, Unmodifiable.class, Implicit.class)
                .map(TypeContext::of);
        List<TypeContext<? extends Annotation>> processed = parameterAnnotationService.sortAndFilter(types);

        Assertions.assertEquals(2, processed.size());
        Assertions.assertEquals(Unmodifiable.class,processed.get(0).type());
        Assertions.assertEquals(Implicit.class,processed.get(1).type());
    }
}
