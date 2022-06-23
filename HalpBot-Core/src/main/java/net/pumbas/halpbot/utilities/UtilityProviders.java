package net.pumbas.halpbot.utilities;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class UtilityProviders {

    @Provider
    public Class<? extends StringTraverser> stringTraverser() {
        return StringTraverser.class;
    }
}
