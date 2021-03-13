package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.CommandManager;
import nz.pumbas.customparameters.Shape;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException, InterruptedException
    {
        registerCustomParamaters();
        HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));
    }

    private static void registerCustomParamaters() {
        CommandManager.registerCustomParameterType(Shape.class);

    }
}
