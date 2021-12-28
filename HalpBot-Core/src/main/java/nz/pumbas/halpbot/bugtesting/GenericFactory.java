package nz.pumbas.halpbot.bugtesting;

public interface GenericFactory<R>
{
    R create(String name);
}
