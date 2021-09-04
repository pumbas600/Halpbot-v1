package nz.pumbas.halpbot.sql;

import java.util.HashMap;
import java.util.Map;

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
    public SqlDriver getDriver(String database) {
        if (!this.hasDriver(database)) {
            this.drivers.put(database, new SQLiteDriver(database));
        }
        return this.drivers.get(database);
    }
}
