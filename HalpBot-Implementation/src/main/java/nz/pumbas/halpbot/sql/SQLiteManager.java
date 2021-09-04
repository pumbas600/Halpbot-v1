package nz.pumbas.halpbot.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SQLiteManager implements SqlManager
{
    private final Map<String, SqlDriver> drivers;

    public SQLiteManager() {
        this.drivers = new HashMap<>();
    }

    @Override
    public boolean hasDriver(String database) {
        return this.drivers.containsKey(database);
    }

    @Override
    public SqlDriver createDriver(String database, long duration, TimeUnit timeUnit) {
        return this.drivers.put(database, new SQLiteDriver(database, duration, timeUnit));
    }

    @Override
    public SqlDriver getDriver(String database) {
        if (!this.hasDriver(database)) {
            this.createDriver(database, -1, TimeUnit.MINUTES);
        }
        return this.drivers.get(database);
    }
}
