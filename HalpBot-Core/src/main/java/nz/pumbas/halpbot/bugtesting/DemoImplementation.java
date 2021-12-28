package nz.pumbas.halpbot.bugtesting;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Binds(DemoInterface.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class DemoImplementation implements DemoInterface
{
    @Getter private String name;
}
