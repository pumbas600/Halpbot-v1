package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.utilities.Utils;

public final class Main
{
    private Main() {}

    public static void main(String[] args) throws LoginException {
        HalpBot halpBot = new HalpBot(Utils.getFirstLineFromFile("Token.txt"));
    }
}
