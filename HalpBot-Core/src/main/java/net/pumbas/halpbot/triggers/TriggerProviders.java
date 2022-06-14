package net.pumbas.halpbot.triggers;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class TriggerProviders {

    @Provider
    public Class<? extends TriggerAdapter> triggerAdapter() {
        return HalpbotTriggerAdapter.class;
    }

    @Provider
    public Class<? extends TriggerContext> triggerContext() {
        return HalpbotTriggerContext.class;
    }
}
