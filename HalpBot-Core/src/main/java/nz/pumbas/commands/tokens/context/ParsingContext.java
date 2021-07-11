package nz.pumbas.commands.tokens.context;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.exceptions.IllegalFormatException;

public class ParsingContext extends InvocationContext
{
    private Class<?> type;

    protected ParsingContext(@NotNull String context)
    {
        super(context);
    }

    public Class<?> getType() {
        return this.type;
    }

    public void assertNext(char character) {
        if (super.getOriginal().charAt(super.getCurrentIndex()) == character) {
            super.incrementIndex();
        }
        else throw new IllegalFormatException(
            String.format("Expected the character %s", character));
    }

    public boolean isNext(char character) {
        return this.isNext(character, false);
    }

    public boolean isNext(char character, boolean stepPast) {
        boolean isNext = super.getOriginal().charAt(super.getCurrentIndex()) == character;

        if (isNext && stepPast) super.incrementIndex();
        return isNext;
    }
}
