package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.tokens.BuiltInTypeToken;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.TokenSyntax;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.halpbot.customparameters.Line;
import nz.pumbas.halpbot.customparameters.Plane;
import nz.pumbas.halpbot.customparameters.Vector3;
import nz.pumbas.utilities.Utilities;

import java.lang.annotation.Annotation;

public class Main
{

    public static void main(String[] args) throws LoginException, ClassNotFoundException {

        System.out.println(Enum.valueOf(TokenSyntax.class, "ARRAY"));
        System.out.println(Enum.valueOf(TokenSyntax.class, "TEST"));

        Class<?>[] types = new Class[] { Double.class, String.class, int[].class };
        String command = TokenManager.generateCommand(new Annotation[3][0], types);

        System.out.println(command);
        System.out.println(TokenManager.parseCommand(command, types));

//         HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));

//        SteamTableManager manager = new SteamTableManager();
//
//        var file = Utilities.parseCSVFile("H2O_Sat.csv");
//        file.forEach(line -> {
//            List<Double> records;
//            try {
//                records = Arrays.stream(line)
//                    .map(Double::parseDouble)
//                    .collect(Collectors.toList());
//            } catch (NumberFormatException e) {
//                return;
//            }
//            manager.insertRecord(SteamInserts.SATURATED, records);
//        });
    }
}
