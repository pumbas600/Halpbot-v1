package nz.pumbas.halpbot;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.utilities.HalpbotUtils;

public final class Main
{
    private Main() {}

    public static void main(String[] args) throws LoginException {
        HalpBot halpBot = new HalpBot(HalpbotUtils.getFirstLineFromFile("Token.txt"));
    }
}
