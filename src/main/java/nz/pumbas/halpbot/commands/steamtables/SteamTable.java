package nz.pumbas.halpbot.commands.steamtables;

import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.commands.steamtables.models.Model;
import nz.pumbas.halpbot.commands.steamtables.models.SaturatedSteamModel;
import nz.pumbas.halpbot.commands.steamtables.models.SuperheatedSteamModel;
import nz.pumbas.utilities.enums.Flag;

public enum SteamTable implements Flag<SteamTable>
{
    NONE(null, "None"),
    SATURATED(SaturatedSteamModel.class, "Saturated Steam"),
    SUPERHEATED(SuperheatedSteamModel.class, "Superheated Steam");

    private final static Map<Class<? extends Model>, SteamTable> ModelMappings = new HashMap<>();

    static {
        for (SteamTable steamTable : values()) {
            if (null != steamTable.getModelType())
                ModelMappings.put(steamTable.getModelType(), steamTable);
        }
    }

    private final Class<? extends Model> modelType;
    private final String displayName;

    SteamTable(Class<? extends Model> modelType, String displayName)
    {
        this.modelType = modelType;
        this.displayName = displayName;
    }

    public static SteamTable of(Class<? extends Model> modelType)
    {
        return ModelMappings.getOrDefault(modelType, NONE);
    }

    public Class<? extends Model> getModelType()
    {
        return this.modelType;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }
}
