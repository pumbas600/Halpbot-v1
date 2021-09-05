package nz.pumbas.halpbot.sql;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;

public interface SQLManager
{
    boolean hasDriver(String database);

    SQLDriver getDriver(String database, SQLConsumer<SQLDriver> creationListener);

    default SQLDriver getDriver(String database) {
        return this.getDriver(database, null);
    }
}
