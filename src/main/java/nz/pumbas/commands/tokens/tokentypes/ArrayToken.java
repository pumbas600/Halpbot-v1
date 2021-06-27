package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.pumbas.commands.exceptions.IllegalCommandException;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;
import nz.pumbas.utilities.Reflect;

public class ArrayToken implements ParsingToken {

    private final boolean isOptional;
    private final Class<?> type;
    private final ParsingToken commandToken;
    private final Object defaultValue;
    private final Annotation[] annotations;

    public ArrayToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this(isOptional, type, defaultValue, new Annotation[0]);
    }

    public ArrayToken(boolean isOptional, Class<?> type, @Nullable String defaultValue, Annotation[] annotations) {
        if (!type.isArray())
            throw new IllegalArgumentException(
                    String.format("The type %s, must be an array to be used in an ArrayToken.", type.getSimpleName()));

        this.isOptional = isOptional;
        this.type = type;
        this.annotations = annotations;
        this.commandToken = this.generateCommandToken(Reflect.getArrayType(this.type));
        this.defaultValue = this.parseDefaultValue(defaultValue);
    }

    /**
     * Generates the {@link CommandToken} for this {@link ArrayToken}.
     *
     * @param arrayType
     *      The {@link Class} of the elements in the array
     *
     * @return The generated {@link CommandToken}
     */
    private ParsingToken generateCommandToken(Class<?> arrayType)
    {
        List<CommandToken> commandTokens = TokenManager.parseCommand(
            "#" + TokenManager.getTypeAlias(arrayType),
            new Class[]{ arrayType },
            new Annotation[1][0]);

        if (1 != commandTokens.size())
            throw new IllegalCommandException(
                String.format("The array token of type %s generated %s command tokens. Expected 1.",
                    arrayType, commandTokens.size()));

        if (!(commandTokens.get(0) instanceof ParsingToken))
            throw new IllegalCommandException(
                String.format("Expected the command token for the array token of type %s to be a parsing token, got " +
                    "%s instead", arrayType, commandTokens.get(0)));

        return (ParsingToken) commandTokens.get(0);
    }

    /**
     * @return If this {@link CommandToken} is optional or not.
     */
    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    public boolean matchesOld(@NotNull InvocationContext invocationToken)
    {
        invocationToken.saveState(this);
        if (Reflect.hasAnnotation(this.getAnnotations(), Implicit.class)
            && this.commandToken.matchesOld(invocationToken)) {

            invocationToken.saveState(this);
            //Loop through as many possible tokens that match.
            while (invocationToken.hasNext()) {
                if (this.commandToken.matchesOld(invocationToken))
                    invocationToken.saveState(this);
                else {
                    invocationToken.restoreState(this);
                    break;
                }
            }
            return true;
        }
        else {
            Optional<String> oArrayParameters = invocationToken.restoreState(this).getNextSurrounded("[", "]");
            if (oArrayParameters.isPresent()) {
                InvocationContext subInvocationToken = InvocationContext.of(oArrayParameters.get());

                while (subInvocationToken.hasNext()) {
                    if (!this.commandToken.matchesOld(subInvocationToken))
                        return false;
                }
                return !subInvocationToken.hasNext();
            }
        }
        return false;
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public Annotation[] getAnnotations()
    {
        return this.annotations;
    }

    /**
     * @return The {@link Class type} of this {@link ParsingToken}
     */
    @Override
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Parses an {@link InvocationContext invocation token} to the type of the {@link ParsingToken}.
     *
     * @param context
     *     The {@link InvocationContext invocation token} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} parsing the {@link InvocationContext invocation token} to the correct type
     */
    @Override
    @Nullable
    public Object parseOld(@NotNull InvocationContext context)
    {
        List<Object> parsedArray = new ArrayList<>();
        context.saveState(this);
        if (Reflect.hasAnnotation(this.getAnnotations(), Implicit.class)
            && this.commandToken.matchesOld(context)) {

            context.restoreState(this);
            do {
                parsedArray.add(this.commandToken.parseOld(context));
                context.saveState(this);

                if (!this.commandToken.matchesOld(context)) {
                    context.restoreState(this);
                    break;
                }
                context.restoreState(this);
            }
            while (context.hasNext());
        }
        else {
            Optional<String> oArrayParameters = context.restoreState(this).getNextSurrounded("[", "]");
            if (oArrayParameters.isEmpty())
                return null;

            InvocationContext subInvocationToken = InvocationContext.of(oArrayParameters.get());


            while (subInvocationToken.hasNext()) {
                parsedArray.add(this.commandToken.parseOld(subInvocationToken));
            }
        }

        return Reflect.toArray(Reflect.getArrayType(this.type), parsedArray);
    }

    /**
     * @return The {@link ParsingToken} of the type of this {@link ArrayToken}
     */
    public ParsingToken getCommandToken()
    {
        return this.commandToken;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    @Nullable
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Parses the context into the type of this {@link ParsingToken}. If the context doesn't match, the
     * {@link Result} will contain a {@link Resource} explaing why.
     *
     * @param context
     *     The {@link InvocationContext}
     *
     * @return An {@link Result} containing the parsed context
     */
    @Override
    public Result<Object> parse(@NotNull InvocationContext context)
    {
        List<Object> parsedArray = new ArrayList<>();
        context.saveState(this);
        Result<Object> parsingResult = Result.empty();
        boolean hasAnnotation = Reflect.hasAnnotation(this.getAnnotations(), Implicit.class);

        if (hasAnnotation && (parsingResult = this.commandToken.parse(context)).hasValue()) {
            parsedArray.add(parsingResult.getValue());
            do {
                context.saveState(this);
                parsingResult = this.commandToken.parse(context);
                if (parsingResult.isValueAbsent()) {
                    context.restoreState(this);
                    break;
                }
                parsedArray.add(parsingResult.getValue());
            }
            while (context.hasNext());
        }
        else {
            Optional<String> oArrayParameters = context.restoreState(this).getNextSurrounded("[", "]");
            if (oArrayParameters.isEmpty()) {
                if (parsingResult.hasReason()) return parsingResult;
                else return Result.of(Resource.get("halpbot.commands.match.array.missingbrackets",
                    TokenManager.getTypeAlias(Reflect.getArrayType(this.getType()))));
            }

            InvocationContext elementContext = InvocationContext.of(oArrayParameters.get());

            while (elementContext.hasNext()) {
                parsingResult = this.commandToken.parse(elementContext);
                if (parsingResult.hasValue())
                    parsedArray.add(parsingResult.getValue());
                else return parsingResult;
            }
        }

        return Result.of(Reflect.toArray(Reflect.getArrayType(this.type), parsedArray));

    }

    @Override
    public String toString() {
        return String.format("ArrayToken{isOptional=%s, type=%s, defaultValue=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
