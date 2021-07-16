package nz.pumbas.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
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

    protected ParsingContext(@NotNull String context, @NotNull Class<?> type,
                             @Nullable AbstractCommandAdapter commandAdapter,
                             @Nullable MessageReceivedEvent event,
                             @NotNull List<Class<? extends Annotation>> annotations)
    {
        super(context);
        this.type = type;
        this.commandAdapter = commandAdapter;
        this.event = event;
        this.annotations = annotations;
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull Class<?> type,
                                    @NotNull Class<? extends Annotation>... annotations) {
        return new ParsingContext("", type, null, null, new ArrayList<>(Arrays.asList(annotations)));
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull String context, @NotNull Class<?> type,
                                    @NotNull Class<? extends Annotation>... annotations) {
        return new ParsingContext(context, type, null, null, new ArrayList<>(Arrays.asList(annotations)));
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
