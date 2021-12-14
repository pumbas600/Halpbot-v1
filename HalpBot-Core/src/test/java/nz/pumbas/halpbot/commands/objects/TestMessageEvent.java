package nz.pumbas.halpbot.commands.objects;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TestMessageEvent extends MessageReceivedEvent
{
    public TestMessageEvent() {
        super(null, 0, new TestMessage());
    }
}
