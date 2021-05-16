package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.utilities.Utilities;

import java.util.Arrays;

public class Main
{

    public static void main(String[] args) throws LoginException, ClassNotFoundException {
//        TokenManager.parseCommand("#Double at <#Double degrees from the x-axis>",
//            new Class[] { Double.class, Double.class})
//                .forEach(System.out::println);

         HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));

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
