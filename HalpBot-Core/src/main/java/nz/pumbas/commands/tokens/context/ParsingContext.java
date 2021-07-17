package nz.pumbas.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.IllegalFormatException;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Reflect;

public class ParsingContext extends InvocationContext
{
    private Class<?> clazz;
    private Type type;
    private AbstractCommandAdapter commandAdapter;
    private MessageReceivedEvent event;
    private List<Class<? extends Annotation>> annotationTypes;

    protected ParsingContext(@NotNull String context)
    {
        super(context);
    }

    protected ParsingContext(@NotNull String context, @NotNull Type type,
                             @Nullable AbstractCommandAdapter commandAdapter,
                             @Nullable MessageReceivedEvent event,
                             @NotNull List<Class<? extends Annotation>> annotationTypes)
    {
        super(context);
        this.clazz = Reflect.wrapPrimative(Reflect.asClass(type));
        this.type = type;
        this.commandAdapter = commandAdapter;
        this.event = event;
        this.annotationTypes = annotationTypes;
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext("", type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)));
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull String context, @NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext(context, type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)));
    }

    public static ParsingContext of(@NotNull String context, @NotNull ParsingToken parsingToken) {
        return new ParsingContext(context, parsingToken.type(), null, null, parsingToken.annotationTypes());
    }

    public static ParsingContext of(@NotNull ParsingToken parsingToken) {
        return of("", parsingToken);
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

    public Class<?> clazz() {
        return this.clazz;
    }

    public Type type() {
        return this.type;
    }

    public void type(Type type) {
        this.type = type;
    }

    public AbstractCommandAdapter commandAdapter()
    {
        return this.commandAdapter;
    }

    public MessageReceivedEvent event()
    {
        return this.event;
    }

    public List<Class<? extends Annotation>> annotationTypes()
    {
        return this.annotationTypes;
    }
}
