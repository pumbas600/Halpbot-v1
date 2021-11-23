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

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.MethodContext;

@Service
@Binds(ConverterHandler.class)
public class HalpbotConverterHandler implements ConverterHandler
{
    private final MultiMap<TypeContext<?>, Converter<?>> converters = new ArrayListMultiMap<>();

    private final Set<TypeContext<?>> nonCommandTypes = HartshornUtils.emptyConcurrentSet();
    private final Set<TypeContext<? extends Annotation>> nonCommandAnnotations = HartshornUtils.emptyConcurrentSet();

    @Override
    public <T> Converter<T> from(ParameterContext<T> parameterContext,
                                 List<TypeContext<? extends Annotation>> sortedAnnotations) {
        TypeContext<?> targetAnnotationType = sortedAnnotations.isEmpty() ? TypeContext.VOID : sortedAnnotations.get(0);
        return this.from(parameterContext.type(), targetAnnotationType);
    }

    @Override
    public void registerConverter(Converter<?> converter) {
        this.converters.put(converter.typeContext(), converter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Converter<T> from(TypeContext<T> typeContext, TypeContext<?> targetAnnotationType) {
        if (!this.converters.containsKey(typeContext))
            return (Converter<T>) DefaultConverters.OBJECT_CONVERTER;

        return (Converter<T>) this.converters.get(typeContext)
                .stream()
                .filter(converter -> converter.annotationType().equals(targetAnnotationType))
                .findFirst()
                .orElse(DefaultConverters.OBJECT_CONVERTER);
    }

    @Override
    public void addNonCommandTypes(Set<TypeContext<?>> types) {
        this.nonCommandTypes.addAll(types);
    }

    @Override
    public void addNonCammandAnnotations(Set<TypeContext<? extends Annotation>> types) {
        this.nonCommandAnnotations.addAll(types);
    }

    @Override
    public boolean isCommandParameter(ParameterContext<?> parameterContext) {
        return !this.isCommandParameter(parameterContext.type()) &&
                this.nonCommandAnnotations
                        .stream()
                        .noneMatch(annotation -> parameterContext.annotation(annotation.type()).present());
    }

    @Override
    public boolean isCommandParameter(TypeContext<?> typeContext) {
        return !this.nonCommandTypes.contains(typeContext);
    }
}
