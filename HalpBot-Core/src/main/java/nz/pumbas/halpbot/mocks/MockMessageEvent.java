package nz.pumbas.halpbot.mocks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

public class MockMessageEvent extends MessageReceivedEvent
{
    public static final MockMessageEvent INSTANCE = new MockMessageEvent();

    public MockMessageEvent() {
        super(MockJDA.INSTANCE, 0, new MockMessage("TEST"));
    }

    public MockMessageEvent(Guild guild, Member member) {
        super(MockJDA.INSTANCE, 0, new MockMessage("TEST", guild, member));
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return this.getMessage().getGuild();
    }
}
