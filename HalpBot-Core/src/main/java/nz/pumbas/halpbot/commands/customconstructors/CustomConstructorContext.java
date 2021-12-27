package nz.pumbas.halpbot.commands.customconstructors;

import nz.pumbas.halpbot.commands.context.TokenActionContext;

public interface CustomConstructorContext extends TokenActionContext
{
    String usage();
}
