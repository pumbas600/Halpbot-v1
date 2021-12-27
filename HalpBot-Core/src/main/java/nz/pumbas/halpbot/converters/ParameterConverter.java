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

package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.Set;

import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

//TODO: Make converters support use of $Default
public interface ParameterConverter<T> extends Converter<CommandInvocationContext, T>, ReflectionConverter
{

    @Override
    @SuppressWarnings("unchecked")
    default Exceptional<T> apply(CommandInvocationContext invocationContext) {

        if (this.requiresHalpbotEvent() && invocationContext.halpbotEvent() == null)
            return Exceptional.of(
                    new NullPointerException("The halpbot event is null but it is required to convert this type"));

        //TODO: Move to state object
        int currentIndex = invocationContext.currentIndex();
        int currentAnnotationIndex = invocationContext.currentAnnotationIndex();
        boolean canHaveContextLeft = invocationContext.canHaveContextLeft();
        TypeContext<?> typeContext = invocationContext.currentType();
        Set<Annotation> annotations = invocationContext.annotations();

        Exceptional<T> result = this.parseReflection(invocationContext).map((obj) -> (T)obj);
        if (result.caught() || result.orNull() == HalpbotUtils.IGNORE_RESULT) {
            invocationContext.currentIndex(currentIndex);
            result = this.mapper().apply(invocationContext)
                    .caught(throwable -> invocationContext.currentIndex(currentIndex));
        }

        // Always restore the state of parser back to what it was when it was called.
        invocationContext.currentAnnotationIndex(currentAnnotationIndex);
        invocationContext.currentType(typeContext);
        invocationContext.canHaveContextLeft(canHaveContextLeft);
        invocationContext.annotations(annotations);
        return result;
    }
}
