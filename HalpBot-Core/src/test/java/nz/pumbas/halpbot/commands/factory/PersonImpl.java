package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

@Binds(Person.class)
public record PersonImpl(String name, int age)
        implements Person
{
    @Bound
    public PersonImpl {}
}
