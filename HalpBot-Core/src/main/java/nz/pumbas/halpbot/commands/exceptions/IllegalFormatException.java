package nz.pumbas.halpbot.commands.exceptions;

public class IllegalFormatException extends IllegalArgumentException
{
    public IllegalFormatException() {
        super();
    }

    public IllegalFormatException(String message) {
        super(message);
    }
}
