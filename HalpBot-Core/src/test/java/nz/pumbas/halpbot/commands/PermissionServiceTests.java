package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.mocks.MockMember;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.mocks.MockJDA;
import nz.pumbas.halpbot.mocks.MockMessageEvent;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.Merger;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.permissions.PermissionSupplier;
import nz.pumbas.halpbot.permissions.Permissions;

@Service
@HartshornTest
@UseCommands
public class PermissionServiceTests
{
    private final long randomMemberId = 2;
    public static final String RANDOM_MEMBER = "halpbot.test.randommember";

    @Inject private PermissionService permissionService;
    @Inject private HalpbotCore halpbotCore;
    @Inject private CommandAdapter commandAdapter;
    @Inject private InvocationContextFactory factory;

    private final GuildImpl guild = new GuildImpl(MockJDA.INSTANCE, 1);
    private Member guildOwner;
    private Member botOwner;
    private Member randomMember;

    @PermissionSupplier(RANDOM_MEMBER)
    public boolean isRandomMember(Guild guild, Member member) {
        return member.getIdLong() == this.randomMemberId;
    }

    @BeforeEach
    public void enable() {
        this.guildOwner = new MockMember(this.guild, new UserImpl(1, MockJDA.INSTANCE));
        this.botOwner = new MockMember(this.guild, new UserImpl(this.halpbotCore.ownerId(), MockJDA.INSTANCE));
        this.randomMember = new MockMember(this.guild, new UserImpl(this.randomMemberId, MockJDA.INSTANCE));

        this.guild.setOwnerId(this.guildOwner.getIdLong());
    }

    @Test
    public void automaticallyRegisteringPermissonsTest() {
        Assertions.assertTrue(this.permissionService.isRegistered(RANDOM_MEMBER));
    }

    @Test
    public void guildOwnerPermissionTest() {
        Assertions.assertTrue(this.permissionService.hasPermission(
                this.guild, this.guildOwner, HalpbotPermissions.GUILD_OWNER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                this.guild, this.botOwner, HalpbotPermissions.GUILD_OWNER));
        Assertions.assertFalse(this.permissionService.hasPermission(
                this.guild, this.randomMember, HalpbotPermissions.GUILD_OWNER));
    }

    @Test
    public void botOwnerPermissionTest() {
        Assertions.assertFalse(this.permissionService.hasPermission(
                this.guild, this.guildOwner, HalpbotPermissions.BOT_OWNER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                this.guild, this.botOwner, HalpbotPermissions.BOT_OWNER));
        Assertions.assertFalse(this.permissionService.hasPermission(
                this.guild, this.randomMember, HalpbotPermissions.BOT_OWNER));
    }

    @Test
    public void customPermissionTest() {
        Assertions.assertFalse(this.permissionService.hasPermission(
                this.guild, this.guildOwner, RANDOM_MEMBER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                this.guild, this.botOwner, RANDOM_MEMBER));
        Assertions.assertTrue(this.permissionService.hasPermission(
                this.guild, this.randomMember, RANDOM_MEMBER));
    }

    private HalpbotEvent createEvent(Member member) {
        return new MessageEvent(new MockMessageEvent(this.guild, member));
    }

    @Test
    public void orPermissionTest() {
        final Member anotherMember = new MockMember(this.guild, new UserImpl(3, MockJDA.INSTANCE));
        final CommandContext commandContext = this.commandAdapter.commandContext("orPermissionTest");
        Assertions.assertNotNull(commandContext);

        Assertions.assertTrue(commandContext.invoke(
                this.factory.command("1 3", this.createEvent(this.guildOwner)))
                .present());
        Assertions.assertTrue(commandContext.invoke(
                this.factory.command("1 3", this.createEvent(this.botOwner)))
                .present());
        Assertions.assertTrue(commandContext.invoke(
                this.factory.command("1 3", this.createEvent(this.randomMember)))
                .present());
        Assertions.assertFalse(commandContext.invoke(
                this.factory.command("1 3", this.createEvent(anotherMember)))
                .present());
    }

    @Test
    public void singlePermissionTest() {
        final CommandContext commandContext = this.commandAdapter.commandContext("singlePermissionTest");
        Assertions.assertNotNull(commandContext);

        Assertions.assertTrue(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.guildOwner)))
                .present());
        Assertions.assertTrue(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.botOwner)))
                .present());
        Assertions.assertFalse(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.randomMember)))
                .present());
    }

    @Test
    public void andPermissionTest() {
        final CommandContext commandContext = this.commandAdapter.commandContext("andPermissionTest");
        Assertions.assertNotNull(commandContext);

        Assertions.assertFalse(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.guildOwner)))
                .present());
        Assertions.assertTrue(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.botOwner)))
                .present());
        Assertions.assertFalse(commandContext.invoke(
                        this.factory.command("1 3", this.createEvent(this.randomMember)))
                .present());
    }

    @Permissions(permissions = {RANDOM_MEMBER, HalpbotPermissions.GUILD_OWNER}, merger = Merger.OR)
    @Command(alias = "orPermissionTest", description = "Tests that the @Permissions annotation works")
    public int orPermissionTest(int a, int b) {
        return a + b;
    }

    @Permissions(permissions = HalpbotPermissions.GUILD_OWNER)
    @Command(alias = "singlePermissionTest", description = "Tests that the @Permissions annotation works")
    public int singlePermissionTest(int a, int b) {
        return a + b;
    }

    @Permissions(permissions = {RANDOM_MEMBER, HalpbotPermissions.GUILD_OWNER})
    @Command(alias = "andPermissionTest", description = "Tests that the @Permissions annotation works")
    public int andPermissionTest(int a, int b) {
        return a + b;
    }
}
