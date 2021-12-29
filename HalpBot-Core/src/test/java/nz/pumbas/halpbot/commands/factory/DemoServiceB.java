package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import javax.inject.Inject;

import lombok.Getter;

@Service
@Binds(DemoServiceB.class)
public class DemoServiceB
{
    @Getter
    @Inject private DemoServiceA serviceA;
}
