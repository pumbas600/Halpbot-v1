package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Base64;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.processes.PipeProcess;
import nz.pumbas.halpbot.utilities.enums.StatusCode;

public class CalculusCommands
{
    private final PipeProcess process;

    public CalculusCommands(PipeProcess process) {
        this.process = process;
    }

    @Command(alias = "display", command = "ANY")
    public void onDisplay(MessageReceivedEvent event, String expression)
    {
        this.handleImagePipeRequest(event, String.format("display, %s", this.formatExpression(expression)));
    }

    @Command(alias = "differentiate", command = "ANY <with respect to> WORD")
    public void onDifferentiate(MessageReceivedEvent event, String expression, String variable)
    {
        this.handleImagePipeRequest(event,
                String.format("differentiate, %s, %s", this.formatExpression(expression), variable));
    }

    @Command(alias = "integrate", command = "ANY <with respect to> WORD")
    public void onIntegrate(MessageReceivedEvent event, String expression, String variable)
    {
        this.handleImagePipeRequest(event,
                String.format("integrate, %s, %s", this.formatExpression(expression), variable));
    }

    private String formatExpression(String expression)
    {
        return expression.replace("^", "**");
    }

    private void handleImagePipeRequest(MessageReceivedEvent event, String request)
    {
        String filename = event.getAuthor().getId() + ".png";
        StatusCode statusCode = this.process.pipeRequest(request);

        if (statusCode.isSuccessful()) {
            String b64ImageEncoding = this.process.getResponse();
            byte[] data = this.convertPythonB64ImageEncoding(b64ImageEncoding);
            event.getChannel().sendFile(data, filename).queue();
        }
        else throw new ErrorMessageException(
                String.format("There was an error trying to display the expression, make sure the formatting is " +
                        "correct. Error Code: [%s]", statusCode.getCode()));
    }

    private byte[] convertPythonB64ImageEncoding(String b64ImageEncoding)
    {
        //Strips the b' prefix and the ending '
        String encoding = b64ImageEncoding.substring(2, b64ImageEncoding.length() - 1);
        return Base64.getDecoder().decode(encoding);
    }

    @Command(alias= "test")
    public void onTest(MessageReceivedEvent event)
    {
        String bytes = "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";
        byte[] byteArray = Base64.getDecoder().decode(bytes);
        event.getChannel().sendFile(byteArray, "test.png").queue();
    }
}
