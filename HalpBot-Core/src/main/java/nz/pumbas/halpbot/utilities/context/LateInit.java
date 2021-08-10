package nz.pumbas.halpbot.utilities.context;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface LateInit
{
    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    void lateInitialisation();
}
