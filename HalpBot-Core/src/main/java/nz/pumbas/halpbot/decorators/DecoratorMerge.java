package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public enum DecoratorMerge
{
    KEEP_PARENT((action, parent) -> parent),
    KEEP_ACTION((action, parent) -> action),
    KEEP_BOTH((action, parent) -> {
        List<Annotation> merged = new ArrayList<>(action);
        merged.addAll(parent);
        return merged;
    });

    private final BiFunction<List<Annotation>, List<Annotation>, List<Annotation>> merger;

    DecoratorMerge(BiFunction<List<Annotation>, List<Annotation>, List<Annotation>> merger) {
        this.merger = merger;
    }

    public List<Annotation> merge(List<Annotation> action, List<Annotation> parent) {
        return this.merger.apply(action, parent);
    }
}
