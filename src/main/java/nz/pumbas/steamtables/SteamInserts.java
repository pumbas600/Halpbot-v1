package nz.pumbas.steamtables;

public enum SteamInserts
{
    SATURATED("INSERT INTO saturated(pressure, temperature, volumeliquid, volumevapour, internalenergyliquid, " +
        "internalenergyvapour, enthalpyliquid, enthalpyvapour, entropyliquid, entropyvapour) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?)");

    private final String sql;

    SteamInserts(String sql) {
        this.sql = sql;
    }

    public String getSql()
    {
        return this.sql;
    }
}
