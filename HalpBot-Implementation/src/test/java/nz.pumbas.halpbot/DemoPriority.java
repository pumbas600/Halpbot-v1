package nz.pumbas.halpbot;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nz.pumbas.halpbot.bugtesting.DemoInterface;

@Binds(value = DemoInterface.class, priority = 0)
@AllArgsConstructor(onConstructor_ = @Bound)
public class DemoPriority implements DemoInterface
{
    @Getter private String name;
}
