package nz.pumbas.halpbot.sql;

import java.util.concurrent.TimeUnit;

public interface SqlManager
{
    boolean hasDriver(String database);

    SqlDriver createDriver(String database, long delay, TimeUnit timeUnit);

    SqlDriver getDriver(String database);
}
