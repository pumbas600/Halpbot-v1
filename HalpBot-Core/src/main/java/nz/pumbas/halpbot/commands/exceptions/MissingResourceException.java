package nz.pumbas.halpbot.commands.exceptions;

public class MissingResourceException extends RuntimeException
{
    public MissingResourceException(String message) {
        super(message);
    }
}
