package nz.pumbas.halpbot.permissions.exceptions;

public class InsufficientPermissionException extends RuntimeException
{
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
