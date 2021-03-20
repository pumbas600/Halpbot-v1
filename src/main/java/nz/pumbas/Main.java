package nz.pumbas;

import javax.security.auth.login.LoginException;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException
    {
        HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));
    }
}
