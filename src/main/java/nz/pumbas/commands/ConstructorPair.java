package nz.pumbas.commands;

import java.lang.reflect.Constructor;

public class ConstructorPair
{
    public String constructorRegex;
    public Constructor<?> constructor;

    public ConstructorPair(String constructorRegex, Constructor<?> constructor)
    {
        this.constructorRegex = constructorRegex;
        this.constructor = constructor;
    }
}
