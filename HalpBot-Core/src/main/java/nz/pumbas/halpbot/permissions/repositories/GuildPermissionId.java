package nz.pumbas.halpbot.permissions.repositories;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuildPermissionId
{
    private long guildId;
    private String permission;

    @Override
    public int hashCode() {
        return Objects.hash(this.guildId, this.permission);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        return obj instanceof GuildPermissionId key
                && key.guildId() == this.guildId
                && key.permission().equals(this.permission);
    }
}
