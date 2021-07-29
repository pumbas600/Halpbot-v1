package nz.pumbas.commands.tokens.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nz.pumbas.utilities.Reflect;

public class ContextState
{
    public static final ContextState EMPTY = new ContextState(null, new Annotation[0], Collections.emptyList());

    private Type type;
    private Annotation[] annotations;
    private List<Class<? extends Annotation>> annotationTypes;

    public ContextState(Type type, Annotation[] annotations, List<Class<? extends Annotation>> annotationTypes)
    {
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
        if (this.equals(EMPTY))
            return this;
        return new ContextState(this.type, this.annotations, new ArrayList<>(this.annotationTypes));
    }

    public void type(Type type)
    {
        this.type = type;
    }

    public void annotations(Annotation[] annotations)
    {
        this.annotations = annotations;
    }

    public void annotationTypes(List<Class<? extends Annotation>> annotationTypes)
    {
        this.annotationTypes = annotationTypes;
    }

    public Type type()
    {
        return this.type;
    }

    public Annotation[] annotations()
    {
        return this.annotations;
    }

    public List<Class<? extends Annotation>> annotationTypes()
    {
        return this.annotationTypes;
    }

    public Class<?> clazz() {
        return Reflect.wrapPrimative(Reflect.asClass(this.type));
    }
}
