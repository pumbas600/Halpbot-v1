package nz.pumbas.halpbot.commands.exceptions;

public class IllegalCustomParameterException extends IllegalArgumentException
{

    public IllegalCustomParameterException(String message) {
        super(message);
    }
}
