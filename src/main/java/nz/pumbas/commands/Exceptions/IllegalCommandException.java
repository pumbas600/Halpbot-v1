package nz.pumbas.commands.Exceptions;

public class IllegalCommandException extends IllegalArgumentException
{

    public IllegalCommandException(String message)
    {
        super(message);
    }
}
