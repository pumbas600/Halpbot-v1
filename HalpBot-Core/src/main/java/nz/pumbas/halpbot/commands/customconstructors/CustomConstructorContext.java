package nz.pumbas.halpbot.commands.customconstructors;

import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.TokenInvokable;

public interface CustomConstructorContext extends TokenInvokable
{
    @Override
    @Nullable
    default Object instance() {
        return null;
    }

    String usage();
}
