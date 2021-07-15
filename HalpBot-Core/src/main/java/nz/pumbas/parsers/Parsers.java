package nz.pumbas.parsers;

import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;
import nz.pumbas.utilities.Reflect;

@SuppressWarnings({"rawtypes"})
public final class Parsers
{
    public static final TypeParser<Number> NUMBER_PARSER = TypeParser.of(Number.class, ctx -> {
        String token = ctx.getNext();

        Result<Object> result = Reflect.matches(token, ctx.getType())
            ? Result.of(Reflect.parse(token, ctx.getType()))
            : Result.of(Resource.get("halpbot.commands.match.builtintype",
            token, ctx.getType().getSimpleName()));

        return result.cast();
    });

    public static final TypeParser<Enum> ENUM_PARSER = TypeParser.of(Enum.class, ctx -> {
        String token = ctx.getNext();

        return Result.of(Reflect.parseEnumValue(ctx.getType(), token.toUpperCase()),
            Resource.get("halpbot.commands.match.enum", token, ctx.getType().getSimpleName()));
    });

    private Parsers() {}
}
