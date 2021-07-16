package nz.pumbas.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nz.pumbas.commands.annotations.Unmodifiable;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.parsers.Parsers;
import nz.pumbas.parsers.TypeParser;
import nz.pumbas.utilities.Exceptional;

public class ParserTests
{

    @Test
    public void retrievingIntegerParserTest() {
        TypeParser<Integer> integerParser =
            Parsers.retrieveParser(Integer.class, ParsingContext.of(Integer.class));

        Assertions.assertEquals(Parsers.INTEGER_PARSER, integerParser);
    }

    @Test
    public void retrievingListParserTest() {
        TypeParser<List> listParser =
            Parsers.retrieveParser(List.class, ParsingContext.of(List.class));

        Assertions.assertEquals(Parsers.LIST_PARSER, listParser);
    }

    @Test
    public void retrievingSetParserTest() {
        TypeParser<Set> setParser =
            Parsers.retrieveParser(Set.class, ParsingContext.of(Set.class));

        Assertions.assertEquals(Parsers.SET_PARSER, setParser);
    }

    @Test
    public void retrievingUnmodifiableListParserTest() {
        TypeParser<List> listParser =
            Parsers.retrieveParser(List.class, ParsingContext.of(List.class, Unmodifiable.class));

        Assertions.assertEquals(Parsers.UNMODIFIABLE_LIST_PARSER, listParser);
    }

    @Test
    public void parsingListTest() {
        Exceptional<List> list = Parsers.LIST_PARSER.getParser()
            .apply(ParsingContext.of("[5 1 3 12 20]",List.class));

        System.out.println(list);
    }
}
