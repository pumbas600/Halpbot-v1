package nz.pumbas.halpbot.mocks;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MockMessageEvent extends MessageReceivedEvent
{
    public static final MockMessageEvent INSTANCE = new MockMessageEvent();

    public MockMessageEvent() {
        super(MockJDA.INSTANCE, 0, new MockMessage("TEST"));
    }
}
