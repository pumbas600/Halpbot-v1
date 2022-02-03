package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.binding.BindingHierarchy;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.data.ValueLookup;
import org.dockbox.hartshorn.data.annotations.UseConfigurations;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@UseConfigurations
@HartshornTest
public class Tests
{

    @Inject
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        BindingHierarchy<ValueLookup> heirarchy = this.applicationContext.hierarchy(Key.of(ValueLookup.class));
        Assertions.assertNotNull(heirarchy);
    }

}
