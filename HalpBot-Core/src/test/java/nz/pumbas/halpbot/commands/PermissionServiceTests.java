package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.mocks.MockJDA;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionService;

@HartshornTest
@UseCommands
public class PermissionServiceTests
{
    @Inject
    private PermissionService permissionService;
    @Inject
    private HalpbotCore halpbotCore;

    @Test
    public void permissionSupplierTest() {
        GuildImpl guild = new GuildImpl(MockJDA.INSTANCE, 1);
        Member guildOwner = new MemberImpl(guild, new UserImpl(1, MockJDA.INSTANCE));
        Member botOwner = new MemberImpl(guild, new UserImpl(this.halpbotCore.ownerId(), MockJDA.INSTANCE));
        Member randomMember = new MemberImpl(guild, new UserImpl(2, MockJDA.INSTANCE));

        guild.setOwnerId(guildOwner.getIdLong());

        Assertions.assertTrue(this.permissionService.hasPermission(
                guild, guildOwner, HalpbotPermissions.GUILD_OWNER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                guild, botOwner, HalpbotPermissions.GUILD_OWNER));
        Assertions.assertFalse(this.permissionService.hasPermission(
                guild, randomMember, HalpbotPermissions.GUILD_OWNER));

        Assertions.assertFalse(this.permissionService.hasPermission(
                guild, guildOwner, HalpbotPermissions.BOT_OWNER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                guild, botOwner, HalpbotPermissions.BOT_OWNER));
        Assertions.assertFalse(this.permissionService.hasPermission(
                guild, randomMember, HalpbotPermissions.BOT_OWNER));
    }
}
