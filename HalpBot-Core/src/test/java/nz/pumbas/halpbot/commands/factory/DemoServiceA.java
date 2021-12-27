package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import javax.inject.Inject;

import lombok.Getter;

@Service
public class DemoServiceA
{
    @Getter
    @Inject private DemoServiceB serviceB;
}
