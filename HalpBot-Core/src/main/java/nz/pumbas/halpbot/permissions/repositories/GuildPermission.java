package nz.pumbas.halpbot.permissions.repositories;

import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuildPermission
{
    private long guildId;
    private String permission;
    private long roleId;
}
