package nz.pumbas.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.IllegalFormatException;

public class ParsingContext extends InvocationContext
{
    private Class<?> type;
    private AbstractCommandAdapter commandAdapter;
    private MessageReceivedEvent event;
    private List<Class<? extends Annotation>> annotations;

    protected ParsingContext(@NotNull String context)
    {
        super(context);
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

    public Class<?> getType() {
        return this.type;
    }

    public AbstractCommandAdapter getCommandAdapter()
    {
        return this.commandAdapter;
    }

    public MessageReceivedEvent getEvent()
    {
        return this.event;
    }

    public List<Class<? extends Annotation>> getAnnotations()
    {
        return this.annotations;
    }
}
