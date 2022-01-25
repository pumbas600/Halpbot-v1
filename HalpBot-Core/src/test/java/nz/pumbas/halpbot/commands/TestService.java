package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;

import javax.inject.Inject;

@Service
public class TestService
{
    @Inject
    private ApplicationContext applicationContext;

    public boolean test(TestService self) {
        return this.equals(self);
    }

}
