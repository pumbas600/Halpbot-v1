package nz.pumbas.halpbot.commands.exceptions;

public class ErrorMessageException extends OutputException
{
    public ErrorMessageException(String message) {
        super(message);
    }
}
