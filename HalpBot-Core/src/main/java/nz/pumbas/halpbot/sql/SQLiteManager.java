package nz.pumbas.halpbot.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.context.LateInit;

public class SQLiteManager implements SQLManager, LateInit
{
    private final Map<String, SQLDriver> drivers;
    private int currentSQLDriver;

    public SQLiteManager() {
        this.drivers = new HashMap<>();
    }

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        HalpbotUtils.context().get(ConcurrentManager.class)
            .scheduleRegularly(10, 10, TimeUnit.MINUTES, this::reloadAllDrivers);
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

    @Override
    public void reloadAllDrivers() {
        this.drivers.values()
            .forEach(SQLDriver::reload);
        HalpbotUtils.logger().info("Reloaded SQL drivers");
    }
}
