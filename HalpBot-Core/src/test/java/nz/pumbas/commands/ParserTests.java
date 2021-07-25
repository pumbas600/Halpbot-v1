package nz.pumbas.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import nz.pumbas.commands.annotations.Remaining;
import nz.pumbas.commands.annotations.Unmodifiable;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.parsers.Parser;
import nz.pumbas.parsers.Parsers;
import nz.pumbas.utilities.Exceptional;

public class ParserTests
{

    @Test
    public void retrievingIntegerParserTest() {
        Parser<Integer> integerParser = Parsers.from(ParsingContext.of(Integer.class));

        Assertions.assertEquals(Parsers.INTEGER_PARSER, integerParser);
    }

    @Test
    public void retrievingListParserTest() {
        Parser<List<?>> listParser = Parsers.from(ParsingContext.of(List.class));

        Assertions.assertEquals(Parsers.LIST_PARSER, listParser);
    }

    @Test
    public void retrievingSetParserTest() {
        Parser<Set<?>> setParser = Parsers.from(ParsingContext.of(Set.class));

        Assertions.assertEquals(Parsers.SET_PARSER, setParser);
    }

    @Test
    public void retrievingUnmodifiableListParserTest() {
        Parser<List<?>> listParser = Parsers.from(ParsingContext.of(List.class, Unmodifiable.class));

        Assertions.assertEquals(Parsers.UNMODIFIABLE_LIST_PARSER, listParser);
    }

    @Test
    public void retrievingRemainingStringsParserTest() {
        Parser<String> remainingStringsParser = Parsers.from(ParsingContext.of(String.class, Remaining.class));

        Assertions.assertEquals(Parsers.REMAINING_STRINGS_PARSER, remainingStringsParser);
    }

    @Test
    public void parsingRemainingStringsTest() {
        ParsingContext ctx = ParsingContext.of("This is a test sentence.", String.class, Remaining.class);
        Parser<String> parser = Parsers.from(ctx);

        Exceptional<String> sentence = parser.apply(ctx);

        Assertions.assertTrue(sentence.present());
        Assertions.assertEquals("This is a test sentence.", sentence.get());
    }

    @Test
    public void parsingArrayTest() {
        ParsingContext ctx = ParsingContext.of("[5 1 3 12 20]",Integer[].class);
        Parser<Integer[]> parser = Parsers.from(ctx);

        Exceptional<Integer[]> array = parser.apply(ctx);

        System.out.println(Arrays.toString(array.get()));
    }
}
