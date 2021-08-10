package nz.pumbas.halpbot.utilities.context;

import nz.pumbas.halpbot.parsers.ParserHandler;
import nz.pumbas.halpbot.parsers.ParserHandlerImpl;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public final class DefaultContext
{
    private DefaultContext() {}

    public static void addAll() {

        // Parsers
        HalpbotUtils.context().bind(ParserHandler.class, ParserHandlerImpl.class);

    }
}
