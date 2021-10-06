package nz.pumbas.halpbot.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SlashCommand
{
    /**
     * This should only be set to true if you've edited or just created the slash command so that its registered.
     * If you've not made any changes, turning this off means that you'll instantly be able to use your slash
     * commands when you start up your bot.
     */
    boolean register() default false;
}
