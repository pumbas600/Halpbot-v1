package nz.pumbas.halpbot.converters.types;

import org.dockbox.hartshorn.core.context.element.TypeContext;

public class ArrayTypeContext extends TypeContext<Object>
{
    public static final ArrayTypeContext TYPE = new ArrayTypeContext();

    protected ArrayTypeContext() {
        super(Object.class);
    }

    //TODO: Override isArray when that's implemented

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        // Return true if this equals any array
        return TYPE.equals(o) || o instanceof TypeContext<?> typeContext && typeContext.type().isArray();
    }

    @Override
    public boolean childOf(TypeContext<?> type) {
        return TYPE == type || super.childOf(type);
    }

    @Override
    public boolean childOf(Class<?> to) {
        return to != null && to.isArray();
    }

    @Override
    public String toString() {
        return "TypeContext{*[]}";
    }
}