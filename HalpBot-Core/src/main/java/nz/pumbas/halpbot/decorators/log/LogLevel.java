package nz.pumbas.halpbot.decorators.log;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

public enum LogLevel
{
    DEBUG(Logger::debug),
    INFO (Logger::info),
    WARN (Logger::warn),
    ERROR(Logger::error);

    private final BiConsumer<Logger, String> logger;

    LogLevel(BiConsumer<Logger, String> logger) {
        this.logger = logger;
    }

    public void log(ApplicationContext applicationContext, String msg) {
        this.logger.accept(applicationContext.log(), msg);
    }
}
