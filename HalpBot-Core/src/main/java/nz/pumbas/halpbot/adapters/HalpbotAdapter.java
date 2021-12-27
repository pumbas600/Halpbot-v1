package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.HalpbotCore;

public interface HalpbotAdapter extends ContextCarrier, Enableable
{
    HalpbotCore halpbotCore();

    default void onCreation(JDA jda) {}

    @Override
    default void enable() throws ApplicationException {
        this.halpbotCore().registerAdapter(this);
    }
}
