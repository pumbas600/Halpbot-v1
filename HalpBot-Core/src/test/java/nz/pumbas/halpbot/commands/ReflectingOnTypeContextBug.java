package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.element.FieldContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ReflectingOnTypeContextBug
{

    // Some TypeContext<?> field
    private final TypeContext<?> type = TypeContext.of(Object.class);

    // Some other field
    private int number = 9;

    @Test
    public void test() {
        List<FieldContext<Integer>> fields = TypeContext.of(ReflectingOnTypeContextBug.class).fieldsOf(int.class);
        Exceptional<Integer> value = fields.get(0).get(this);

        Assertions.assertEquals(1, fields.size());
        Assertions.assertTrue(value.present());
        Assertions.assertEquals(this.number, value.get());
    }
}
