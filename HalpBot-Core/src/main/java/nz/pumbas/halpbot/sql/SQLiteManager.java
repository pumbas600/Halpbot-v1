package nz.pumbas.halpbot.sql;

import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;

public class SQLiteManager implements SQLManager
{
    private final Map<String, SQLDriver> drivers;

    public SQLiteManager() {
        this.drivers = new HashMap<>();
    }

    @Override
    public boolean hasDriver(String database) {
        return this.drivers.containsKey(database);
    }

    @Override
    public SQLDriver getDriver(String database, SQLConsumer<SQLDriver> creationListener) {
        if (!this.hasDriver(database)) {
            SQLiteDriver driver = new SQLiteDriver(database, creationListener);
            this.drivers.put(database, driver);
            driver.lateInitialisation();
        }
        return this.drivers.get(database);
    }
}
