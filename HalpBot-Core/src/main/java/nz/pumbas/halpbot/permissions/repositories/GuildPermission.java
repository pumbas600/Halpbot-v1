package nz.pumbas.halpbot.permissions.repositories;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(GuildPermissionId.class)
public class GuildPermission
{
    @Id
    private long guildId;
    @Id
    private String permission;

    private long roleId;
}
