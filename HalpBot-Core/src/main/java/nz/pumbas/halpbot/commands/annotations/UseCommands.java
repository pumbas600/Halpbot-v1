package nz.pumbas.halpbot.commands.annotations;

import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.commandadapters.HartshornCommandAdapter;

public @interface UseCommands
{
    Class<? extends CommandAdapter> adapter() default HartshornCommandAdapter.class;
}
