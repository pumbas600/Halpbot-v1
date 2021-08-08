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
import nz.pumbas.halpbot.parsers.ParserManager;
import nz.pumbas.halpbot.parsers.Parsers;
import nz.pumbas.halpbot.utilities.Exceptional;

public class ParserTests
{

    @Test
    public void retrievingArrayParserTest() {
        Parser<Object[]> arrayParser = ParserManager.from(MethodContext.of(Object[].class));

        Assertions.assertEquals(Parsers.ARRAY_PARSER, arrayParser);
    }

    @Test
    public void retrievingIntegerParserTest() {
        Parser<Integer> integerParser = ParserManager.from(MethodContext.of(Integer.class));

        Assertions.assertEquals(Parsers.INTEGER_PARSER, integerParser);
    }

    @Test
    public void retrievingListParserTest() {
        Parser<List<?>> listParser = ParserManager.from(MethodContext.of(List.class));

        Assertions.assertEquals(Parsers.LIST_PARSER, listParser);
    }

    @Test
    public void retrievingSetParserTest() {
        Parser<Set<?>> setParser = ParserManager.from(MethodContext.of(Set.class));

        Assertions.assertEquals(Parsers.SET_PARSER, setParser);
    }

    @Test
    public void retrievingUnmodifiableListParserTest() {
        Parser<List<?>> listParser = ParserManager.from(MethodContext.of(List.class, Unmodifiable.class));

        Assertions.assertEquals(Parsers.UNMODIFIABLE_LIST_PARSER, listParser);
    }

    @Test
    public void retrievingRemainingStringsParserTest() {
        Parser<String> remainingStringsParser = ParserManager.from(MethodContext.of(String.class, Remaining.class));

        Assertions.assertEquals(Parsers.REMAINING_STRINGS_PARSER, remainingStringsParser);
    }

    @Test
    public void parsingRemainingStringsTest() {
        MethodContext ctx = MethodContext.of("This is a test sentence.", String.class, Remaining.class);
        Parser<String> parser = ParserManager.from(ctx);

        Exceptional<String> sentence = parser.getMapper().apply(ctx);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @Test
    public void parsingArrayTest() {
        MethodContext ctx = MethodContext.of("[5 1 3 12 20]", Integer[].class);
        Parser<Integer[]> parser = ParserManager.from(ctx);

        Exceptional<Integer[]> array = parser.getMapper().apply(ctx);

        System.out.println(Arrays.toString(array.get()));
    }
}
