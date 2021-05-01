package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.processes.PipeProcess;
import nz.pumbas.utilities.enums.StatusCode;

@CommandGroup(defaultPrefix = "$")
public class CalculusCommands
{
    private final PipeProcess process;

    public CalculusCommands(PipeProcess process) {
        this.process = process;
    }

    @Command(alias = "display", command = "ANY")
    public void onDisplay(MessageReceivedEvent event, String expression) {
        String filename = event.getAuthor().getId() + ".png";
        StatusCode statusCode = this.process.pipeRequest(String.format("display, %s, %s", expression,
            filename));

        if (statusCode.isSuccessful()) {
            File file = new File(filename);
            if (null != file && file.exists()) {
                event.getChannel().sendFile(file).queue();
                file.delete();
                return;
            }
        }
        throw new ErrorMessageException(
            String.format("%s : There was an error trying to display the expression, make sure the formatting is " +
                "correct.", statusCode.getCode()));
    }
}
