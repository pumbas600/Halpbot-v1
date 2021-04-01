package nz.pumbas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.Annotations.Unrequired;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException, NoSuchMethodException
    {
        HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));
//
//        Pattern pattern = Pattern.compile("^(?:([\\w]+))* ?(?:([\\w]+))*");
//        Matcher matcher = pattern.matcher("");
//
//        if (matcher.lookingAt()) {
//            System.out.println(matcher.groupCount());
//        }

        //System.out.println(Unrequired.class.isAssignableFrom(Unrequired.class));

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
