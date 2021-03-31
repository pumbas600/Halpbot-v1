package nz.pumbas;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import nz.pumbas.commands.CommandManager;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.halpbot.HalpBotCommands;
import nz.pumbas.utilities.Utilities;

public class Main
{

    public static void main(String[] args) throws LoginException, NoSuchMethodException
    {
        //HalpBot halpBot = new HalpBot(Utilities.getFirstLineFromFile("Token.txt"));

        Pattern pattern = Pattern.compile("([\\w]+)? (?:([\\w]+))*?");
        Matcher matcher = pattern.matcher("Hi");
        if (matcher.lookingAt()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println(matcher.group(i));
            }
        }

        Optional<String> test = Optional.empty();
        var type =
            (Class<?>)
                (test.getClass()
                    .getGenericSuperclass()).getClass();


        System.out.println(type);

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

    private void test(Optional<String> optional) {

    }
}
