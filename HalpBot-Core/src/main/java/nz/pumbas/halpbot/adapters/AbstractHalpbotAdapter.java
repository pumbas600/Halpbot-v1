package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class AbstractHalpbotAdapter extends ListenerAdapter
{
    protected HalpbotCore halpBotCore;

    public void accept(JDA jda) { }

    public void registerObjects(Object... objects) { }

    public HalpbotCore getHalpbotCore() {
        return this.halpBotCore;
    }

    public void setHalpBotCore(HalpbotCore halpBotCore) {
        this.halpBotCore = halpBotCore;
    }
}
