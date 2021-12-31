package nz.pumbas.halpbot.permissions.repositories;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuildPermission
{
    @Id
    private String guildId;

    private String permission;
    private String roleId;
}
