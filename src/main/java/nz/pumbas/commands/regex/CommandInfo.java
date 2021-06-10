package nz.pumbas.commands.regex;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class CommandInfo
{
    public String regexCommand;
    public String displayCommand;
    public List<Constructor<?>> constructors;

    public CommandInfo(String regexCommand, String displayCommand)
    {
        this.regexCommand = regexCommand;
        this.displayCommand = displayCommand;
        this.constructors = new ArrayList<>();
    }
}
