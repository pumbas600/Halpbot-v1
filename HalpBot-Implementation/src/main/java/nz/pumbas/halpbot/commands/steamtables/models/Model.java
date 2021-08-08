package nz.pumbas.halpbot.commands.steamtables.models;

import nz.pumbas.halpbot.objects.keys.KeyHolder;
import nz.pumbas.halpbot.utilities.Reflect;

public interface Model extends KeyHolder<Model>
{
    default double getDouble(String name)
    {
        return Reflect.getField(this, name, 0D);
    }

    default float getFloat(String name)
    {
        return Reflect.getField(this, name, 0F);
    }

    default int getInteger(String name)
    {
        return Reflect.getField(this, name, 0);
    }

    default String getString(String name)
    {
        return Reflect.getField(this, name, null);
    }

    default char getChar(String name)
    {
        return Reflect.<Character>getField(this, name, null);
    }

    default Object getObject(String name)
    {
        return Reflect.getField(this, name, new Object());
    }
}
