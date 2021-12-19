package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Demo
@Service
@HartshornTest
public class DemoTests
{
    @Test
    public void genericFactoryTest() {
        Exceptional<String> test = this.test(1);
        Assertions.assertTrue(test.present());
        Assertions.assertEquals(1, test.get());
    }

    @SuppressWarnings("unchecked")
    private <T> Exceptional<T> test(Integer test) {
        return Exceptional.of((T) test);
    }


}
