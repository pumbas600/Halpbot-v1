package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Function;

public enum Content
{
    RAW(Message::getContentRaw),
    DISPLAY(Message::getContentDisplay),
    STRIPPED(Message::getContentStripped);

    private final Function<Message, String> parser;

    Content(Function<Message, String> parser) {
        this.parser = parser;
    }

    public String parse(MessageReceivedEvent event) {
        return this.parser.apply(event.getMessage());
    }
}
