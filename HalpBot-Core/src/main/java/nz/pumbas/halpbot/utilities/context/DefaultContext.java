package nz.pumbas.halpbot.utilities.context;

import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.ConverterHandlerImpl;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public final class DefaultContext
{
    private DefaultContext() {}

    public static void addAll() {

        // Parsers
        HalpbotUtils.context().bind(ConverterHandler.class, ConverterHandlerImpl.class);

    }
}
