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

package net.pumbas.halpbot.converters.annotations;

import org.dockbox.hartshorn.core.annotations.Extends;
import org.dockbox.hartshorn.core.annotations.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Extends(Component.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ParameterAnnotation
{
    /**
     * The annotations, which if present, should be processed before this one. Note: That this is equivalent to setting
     * the {@link ParameterAnnotation#before()} of the specified annotations to this.
     */
    Class<? extends Annotation>[] after() default {};

    /**
     * The annotations which if present, should be processed afterAnnotations this one. Note: That this is equivalent to
     * setting the {@link ParameterAnnotation#after()} of the specified annotations to this. The main purpose of this is
     * to allow you to add custom annotations that are processed before the built-in ones, as you don't have access to
     * their respective {@link ParameterAnnotation}.
     */
    Class<? extends Annotation>[] before() default {};

    /**
     * The annotations that cannot be used in conjunction with this one.
     */
    Class<? extends Annotation>[] conflictingAnnotations() default {};

    /**
     * The parameter types that this annotation can be used on. By default, it can be used on any type.
     */
    Class<?>[] allowedType() default Object.class;
}
