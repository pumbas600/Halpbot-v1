package nz.pumbas.commands.Exceptions;

public class ErrorMessageException extends IllegalArgumentException
{
    public ErrorMessageException(String message)
    {
        super(message);
    }
}
