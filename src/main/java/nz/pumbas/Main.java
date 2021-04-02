package nz.pumbas;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.objects.keys.Keys;
import nz.pumbas.objects.keys.StorageKey;
import nz.pumbas.utilities.Test;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException, NoSuchMethodException
    {
        HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));

//        StorageKey<Test, String> NAME = Keys.storageKey();
//        Test test = new Test();
//        test.set(NAME, "Josh");
//        Test test2 = new Test();
//        NAME.set(test2, "pumbas600");
//
//        System.out.println(test.getUnchecked(NAME));
//        System.out.println(NAME.getUnchecked(test2));

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
