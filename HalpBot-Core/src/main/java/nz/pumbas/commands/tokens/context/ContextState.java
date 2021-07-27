package nz.pumbas.commands.tokens.context;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ContextState
{
    private final int currentIndex;
    private final Annotation[] annotations;
    private final List<Class<? extends Annotation>> annotationTypes;

    public ContextState(int currentIndex, Annotation[] annotations, List<Class<? extends Annotation>> annotationTypes)
    {
        this.currentIndex = currentIndex;
        this.annotations = annotations;
        this.annotationTypes = new ArrayList<>(annotationTypes);
    }

    public int currentIndex()
    {
        return this.currentIndex;
    }

    public Annotation[] annotations()
    {
        return this.annotations;
    }

    public List<Class<? extends Annotation>> annotationTypes()
    {
        return this.annotationTypes;
    }
}
