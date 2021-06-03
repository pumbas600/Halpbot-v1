package nz.pumbas.commands.exceptions;

public class IllegalCommandException extends IllegalArgumentException
{

    public IllegalCommandException(String message)
    {
        super(message);
    }
}
