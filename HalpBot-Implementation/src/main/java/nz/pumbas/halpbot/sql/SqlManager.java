package nz.pumbas.halpbot.sql;

public interface SqlManager
{
    boolean hasDriver(String database);

    SqlDriver getDriver(String database);
}
