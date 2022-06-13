package net.pumbas.halpbot.commands.bugs;

import net.dv8tion.jda.api.JDA;
import net.pumbas.halpbot.mocks.MockJDA;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class JDAProvider {

    @Provider(priority = 0)
    public JDA jda() {
        return MockJDA.INSTANCE;
    }
}
