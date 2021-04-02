package nz.pumbas.steamtables.models;

import nz.pumbas.steamtables.annotations.ModelObject;

@ModelObject(tableName = "columns")
public class ColumnModel implements Model
{
    public String columnalias;
    public String columnname;
    public int tables;
}
