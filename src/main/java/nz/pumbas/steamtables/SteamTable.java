package nz.pumbas.steamtables;

import nz.pumbas.steamtables.models.IModel;
import nz.pumbas.steamtables.models.SaturatedSteamModel;
import nz.pumbas.steamtables.models.SuperheatedSteamModel;

public enum SteamTable
{
    SATURATED(SaturatedSteamModel.class, "Saturated Steam"),
    SUPERHEATED(SuperheatedSteamModel.class, "Superheated Steam");

    private final Class<? extends IModel> modelType;
    private final String displayName;

    SteamTable(Class<? extends IModel> modelType, String displayName) {
        this.modelType = modelType;
        this.displayName = displayName;
    }

    public Class<? extends IModel> getModelType() {
        return this.modelType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
