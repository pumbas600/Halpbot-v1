package nz.pumbas.halpbot.commands.customconstructors;

import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.commands.Invokable;

public interface CustomConstructorContext extends Invokable
{
    @Override
    @Nullable
    default Object instance() {
        return null;
    }

    String usage();
}
