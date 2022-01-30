package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.interactions.components.Component;

import java.util.function.Function;

public interface AfterRemovalFunction extends Function<Component, Component>
{
}
