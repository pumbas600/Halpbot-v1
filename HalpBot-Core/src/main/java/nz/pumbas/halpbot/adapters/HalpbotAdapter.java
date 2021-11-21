package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;

import org.dockbox.hartshorn.core.context.ContextCarrier;

public interface HalpbotAdapter extends ContextCarrier
{
    default void onCreation(JDA jda) {}
}
