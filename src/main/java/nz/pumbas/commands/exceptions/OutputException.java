package nz.pumbas.commands.exceptions;

/**
 * A class extended by {@link Exception exceptions} which want to be displayed to the user.
 */
public class OutputException extends IllegalArgumentException
{
    public OutputException(String message) {
        super(message);
    }
}
