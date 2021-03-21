package nz.pumbas.steamtables.models;

import java.util.Map;

import nz.pumbas.steamtables.annotations.Column;

public class ModelInfo
{
    private final String tableName;
    private final Map<String, Column> mappedColumns;

    public ModelInfo(String tableName, Map<String, Column> mappedColumns)
    {
        this.tableName = tableName;
        this.mappedColumns = mappedColumns;
    }

    public String getTableName()
    {
        return this.tableName;
    }

    public Map<String, Column> getMappedColumns()
    {
        return this.mappedColumns;
    }
}
