package nz.pumbas.halpbot.converters.types;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

public class ArrayTypeContext extends TypeContext<Object>
{
    public static final ArrayTypeContext TYPE = new ArrayTypeContext();

    protected ArrayTypeContext() {
        super(Object.class);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null)
            return false;
        // Return true if this equals any array
        return TYPE.equals(o) || o instanceof TypeContext<?> typeContext && typeContext.type().isArray();
    }

    @Override
    public boolean childOf(TypeContext<?> type) {
        return type.isArray();
    }

    @Override
    public boolean childOf(@Nullable Class<?> to) {
        return to != null && to.isArray();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String toString() {
        return "TypeContext{*[]}";
    }
}
