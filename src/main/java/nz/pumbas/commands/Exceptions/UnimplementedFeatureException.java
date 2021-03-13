package nz.pumbas.commands.Exceptions;

public class UnimplementedFeatureException extends IllegalArgumentException
{
    public UnimplementedFeatureException(String message) {
        super(message);
    }
}
