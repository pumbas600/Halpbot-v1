package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.activate.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.dockbox.hartshorn.testsuite.InjectTest;
import org.junit.jupiter.api.Assertions;

@HartshornTest
@Activator(scanPackages = "nz.pumbas.halpbot")
public class PersonFactoryTests
{
    @InjectTest
    public void factoryCreation(PersonFactory personFactory) {
        Person person = personFactory.create("pumbas600", 19);

        Assertions.assertNotNull(person);
        Assertions.assertTrue(person instanceof PersonImpl);
        Assertions.assertEquals("pumbas600", person.name());
        Assertions.assertEquals(19, person.age());
    }

    @InjectTest
    public void defaultFactoryCreation(PersonFactory personFactory) {
        Person person = personFactory.create("pumbas600");

        Assertions.assertNotNull(person);
        Assertions.assertTrue(person instanceof PersonImpl);
        Assertions.assertEquals("pumbas600", person.name());
        Assertions.assertEquals(-1, person.age());
    }
}
