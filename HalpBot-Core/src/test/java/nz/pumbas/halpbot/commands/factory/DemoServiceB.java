package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

@Singleton
@ComponentBinding(value = DemoServiceB.class)
public class DemoServiceB
{
    @Getter
    @Inject private DemoServiceA serviceA;
}
