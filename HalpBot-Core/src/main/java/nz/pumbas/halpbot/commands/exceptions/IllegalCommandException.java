package nz.pumbas.halpbot.commands.exceptions;

public class IllegalCommandException extends IllegalArgumentException
{

    public IllegalCommandException(String message) {
        super(message);
    }
}
