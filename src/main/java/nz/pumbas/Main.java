package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.tokens.*;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.halpbot.customparameters.Line;
import nz.pumbas.halpbot.customparameters.Plane;
import nz.pumbas.halpbot.customparameters.Vector3;
import nz.pumbas.utilities.Utilities;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class Main
{

    public static void main(String[] args) throws LoginException, ClassNotFoundException {
        System.out.println(TokenManager.splitInvocationTokens("This is a 3 element array [2 1 -6]").toString());

        ParsingToken token = new ArrayToken(false, int[].class, "[2 1]"); //TODO: Check default matching error
        System.out.println(token.matches("[1 2 a alpha]"));
        System.out.println(Arrays.toString((Object[]) token.parse("[2 3 4 5]")));
        System.out.println(Arrays.toString((Object[]) token.getDefaultValue()));

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
