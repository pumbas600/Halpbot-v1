package nz.pumbas.halpbot.commands.bugtesting;

public interface GenericFactory<R>
{
    R create(String name);
}
