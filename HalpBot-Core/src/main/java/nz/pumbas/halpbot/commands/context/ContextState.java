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

package nz.pumbas.halpbot.commands.context;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.utilities.Reflect;

//TODO: Remove
public class ContextState
{
    public static final ContextState EMPTY = new ContextState(null, new Annotation[0], Collections.emptyList());

    private Type type;
    private @NotNull Annotation[] annotations;
    private @NotNull List<Class<? extends Annotation>> annotationTypes;

    public ContextState(Type type, @NotNull Annotation[] annotations,
                        @NotNull List<Class<? extends Annotation>> annotationTypes) {
        this.type = type;
        this.annotations = annotations;
        this.annotationTypes = new ArrayList<>(annotationTypes);
    }

    /**
     * Creates a copy of the context state, unless this is the {@link ContextState#EMPTY} context state, in which
     * case it just returns itself. Otherwise, changes to the type, annotation array or annotation types list won't be
     * reflected in the copy, but changes to the objects in the collections will.
     *
     * @return A copy of this {@link ContextState}
     */
    public ContextState copyContextState() {
        if (null == this.type) //This is the EMPTY ContextState.
            return this;
        return new ContextState(this.type, this.annotations, new ArrayList<>(this.annotationTypes));
    }

    /**
     * Sets the {@link Type} of this {@link ContextState}.
     *
     * @param type
     *     The type to set
     */
    public void setType(@NotNull Type type) {
        this.type = type;
    }

    /**
     * Sets the {@link Annotation annotations} for this {@link ContextState}.
     *
     * @param annotations
     *      The array of annotations to set
     */
    public void setAnnotations(@NotNull Annotation[] annotations) {
        this.annotations = annotations;
    }

    /**
     * Sets the annotation types for this {@link ContextState}.
     *
     * @param annotationTypes
     *     The list of annotation types to set
     */
    public void setAnnotationTypes(@NotNull List<Class<? extends Annotation>> annotationTypes) {
        this.annotationTypes = annotationTypes;
    }

    /**
     * @return The {@link Type} of this {@link ContextState}
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return An array of the {@link Annotation annotations} that annotate the current {@link ParsingToken}
     */
    public @NotNull Annotation[] getAnnotations() {
        return this.annotations;
    }

    /**
     * @return A list of the {@link Class annotation types} that annotate the current {@link ParsingToken}
     */
    public @NotNull List<Class<? extends Annotation>> getAnnotationTypes() {
        return this.annotationTypes;
    }

    /**
     * @return The {@link Type} wrapped as a class
     */
    public @NotNull Class<?> getClazz() {
        return Void.class; //Reflect.wrapPrimative(Reflect.asClass(this.type));
    }
}
