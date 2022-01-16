package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import javax.inject.Inject;

import lombok.Getter;

@Singleton
@ComponentBinding(DemoServiceB.class)
public class DemoServiceB
{
    @Getter
    @Inject private DemoServiceA serviceA;
}
