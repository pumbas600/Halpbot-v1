package nz.pumbas.halpbot.commands.steamtables.models;

import nz.pumbas.objects.keys.KeyHolder;
import nz.pumbas.utilities.Utilities;

public interface Model extends KeyHolder<Model>
{
    default double getDouble(String name)
    {
        return Utilities.getField(this, name, 0D);
    }

    default float getFloat(String name)
    {
        return Utilities.getField(this, name, 0F);
    }

    default int getInteger(String name)
    {
        return Utilities.getField(this, name, 0);
    }

    default String getString(String name)
    {
        return Utilities.getField(this, name, null);
    }

    default char getChar(String name)
    {
        return Utilities.<Character>getField(this, name, null);
    }

    default Object getObject(String name)
    {
        return Utilities.getField(this, name, new Object());
    }
}
