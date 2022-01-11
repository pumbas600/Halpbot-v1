package nz.pumbas.halpbot.commands.mocks;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.entities.GuildImpl;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.halpbot.mocks.MockJDA;

public class MockGuild extends GuildImpl
{
    private final Member selfMember;

    public MockGuild(long id, Permission... selfMemberPermissions) {
        super(MockJDA.INSTANCE, id);
        this.selfMember = new MockMember(this, MockJDA.INSTANCE.getSelfUser(), selfMemberPermissions);
    }

    @NotNull
    @Override
    public Member getSelfMember() {
        return this.selfMember;
    }
}
