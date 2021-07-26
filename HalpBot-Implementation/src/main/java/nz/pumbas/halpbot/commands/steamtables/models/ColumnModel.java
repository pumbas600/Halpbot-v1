package nz.pumbas.halpbot.commands.steamtables.models;

import nz.pumbas.halpbot.commands.steamtables.annotations.ModelObject;

@ModelObject(tableName = "columns")
public class ColumnModel implements Model
{
    public String columnalias;
    public String columnname;
    public int tables;
}
