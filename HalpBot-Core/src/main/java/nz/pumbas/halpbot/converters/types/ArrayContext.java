package nz.pumbas.halpbot.converters.types;

import org.dockbox.hartshorn.core.context.element.TypeContext;

public class ArrayContext extends TypeContext<Void>
{
    public static final ArrayContext TYPE = new ArrayContext();

    protected ArrayContext() {
        super(Void.class);
    }

    //TODO: Override isArray when that's implemented

    @Override
    public boolean equals(Object o) {
        // Return true if this equals any array
        return TYPE.equals(o) || o instanceof TypeContext<?> typeContext && typeContext.type().isArray();
    }
}
