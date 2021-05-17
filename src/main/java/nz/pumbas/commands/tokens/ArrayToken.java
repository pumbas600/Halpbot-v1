package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

public class ArrayToken implements CommandToken, ParsingToken {

    private final boolean isOptional;
    private final Class<?> arrayType;

    public ArrayToken(boolean isOptional, Class<?> arrayType) {
        this.isOptional = isOptional;
        this.arrayType = arrayType;
    }


    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public boolean matches(@NotNull String invocationToken) {
        return false;
    }

    @Override
    public Class<?> getType() {
        return this.arrayType;
    }

    @Override
    public Object parse(@NotNull String invocationToken) {
        return null;
    }
}
