package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Base64;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.utilities.enums.StatusCode;

public class CalculusCommands
{

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
//        String filename = event.getAuthor().getId() + ".png";
//        StatusCode statusCode = this.process.pipeRequest(request);
//
//        if (statusCode.isSuccessful()) {
//            String b64ImageEncoding = this.process.getResponse();
//            byte[] data = this.convertPythonB64ImageEncoding(b64ImageEncoding);
//            event.getChannel().sendFile(data, filename).queue();
//        }
//        else throw new ErrorMessageException(
//                String.format("There was an error trying to display the expression, make sure the formatting is " +
//                        "correct. Error Code: [%s]", statusCode.getCode()));
    }

    private byte[] convertPythonB64ImageEncoding(String b64ImageEncoding)
    {
        //Strips the b' prefix and the ending '
        String encoding = b64ImageEncoding.substring(2, b64ImageEncoding.length() - 1);
        return Base64.getDecoder().decode(encoding);
    }
}
