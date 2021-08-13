package nz.pumbas.halpbot.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Unmodifiable;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.parsers.Parser;
import nz.pumbas.halpbot.parsers.ParserHandler;
import nz.pumbas.halpbot.parsers.Parsers;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ParserTests
{

    @Test
    public void retrievingArrayParserTest() {
        Parser<Object[]> arrayParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(Object[].class));

        Assertions.assertEquals(Parsers.ARRAY_PARSER, arrayParser);
    }

    @Test
    public void retrievingIntegerParserTest() {
        Parser<Integer> integerParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(Integer.class));

        Assertions.assertEquals(Parsers.INTEGER_PARSER, integerParser);
    }

    @Test
    public void retrievingListParserTest() {
        Parser<List<?>> listParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(List.class));

        Assertions.assertEquals(Parsers.LIST_PARSER, listParser);
    }

    @Test
    public void retrievingSetParserTest() {
        Parser<Set<?>> setParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(Set.class));

        Assertions.assertEquals(Parsers.SET_PARSER, setParser);
    }

    @Test
    public void retrievingUnmodifiableListParserTest() {
        Parser<List<?>> listParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(List.class, Unmodifiable.class));

        Assertions.assertEquals(Parsers.UNMODIFIABLE_LIST_PARSER, listParser);
    }

    @Test
    public void retrievingRemainingStringsParserTest() {
        Parser<String> remainingStringsParser = HalpbotUtils.context().get(ParserHandler.class)
            .from(MethodContext.of(String.class, Remaining.class));

        Assertions.assertEquals(Parsers.REMAINING_STRINGS_PARSER, remainingStringsParser);
    }

    @Test
    public void parsingRemainingStringsTest() {
        MethodContext ctx = MethodContext.of("This is a test sentence.", String.class, Remaining.class);
        Parser<String> parser = HalpbotUtils.context().get(ParserHandler.class).from(ctx);

        Exceptional<String> sentence = parser.getMapper().apply(ctx);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @Test
    public void parsingArrayTest() {
        MethodContext ctx = MethodContext.of("[5 1 3 12 20]", Integer[].class);
        Parser<Integer[]> parser = HalpbotUtils.context().get(ParserHandler.class).from(ctx);

        Exceptional<Integer[]> array = parser.getMapper().apply(ctx);

        System.out.println(Arrays.toString(array.get()));
    }
}
