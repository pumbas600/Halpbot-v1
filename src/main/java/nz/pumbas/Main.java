package nz.pumbas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import nz.pumbas.steamtables.SteamInserts;
import nz.pumbas.steamtables.SteamTableManager;
import nz.pumbas.utilities.Singleton;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException
    {
        HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));

//        StringBuilder stringBuilder = new StringBuilder(String.format("|%15s|", "Temperature"));
//        System.out.println(stringBuilder);

//        SteamTableManager manager = Singleton.getInstance(SteamTableManager.class);
//        Connection connection = manager.connect();
//        try {
//            String whereColumn = "Temperature";
//            PreparedStatement statement = connection.prepareStatement("SELECT * FROM saturated WHERE " + whereColumn +
//                " = ?");
//            statement.setDouble(1, 60D);
//            ResultSet result = statement.executeQuery();
//            while (result.next()) {
//                System.out.println(result.getString(whereColumn) + "\t" + result.getDouble("Pressure"));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
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
