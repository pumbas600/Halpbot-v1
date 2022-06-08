package net.pumbas.halpbot.commands;

import org.dockbox.hartshorn.application.Activator;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HartshornTest
@Activator(scanPackages = "net.pumbas.halpbot")
public class Tests
{
    @Test
    public void test() {
        Assertions.assertTrue(true);
    }
}
