package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;

public interface HalpbotAdapter
{
    default void accept(JDA jda) { }
}
