package nz.pumbas.halpbot.commands.exceptions;

public class IllegalPersistantDataConstructor extends RuntimeException
{
    public IllegalPersistantDataConstructor(String message) {
        super(message);
    }
}
