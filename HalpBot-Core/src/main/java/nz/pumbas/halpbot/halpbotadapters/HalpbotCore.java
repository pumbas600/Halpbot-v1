package nz.pumbas.halpbot.halpbotadapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

public class HalpbotCore
{
    private static JDA jda;
    private final JDABuilder jdaBuilder;
    private final List<HalpbotAdapter> adapters = new ArrayList<>();

    public HalpbotCore(final JDABuilder jdaBuilder) {
        this.jdaBuilder = jdaBuilder;
    }

    /**
     * @return The {@link JDA} instance
     */
    public static JDA getJDA() {
        return jda;
    }

    /**
     * Adds the {@link HalpbotAdapter}s to the core. This will automatically register the adapters when you invoke
     * {@link HalpbotCore#registerAdapters()}. Note that the Halpbot adapters will also need to extend
     * {@link ListenerAdapter}.
     *
     * @param adapters
     *      The Halpbot adapters to add
     * @param <T>
     *      The type of the Halpbot adapters
     *
     * @return Itself for chaining
     */
    @SafeVarargs
    public final <T extends ListenerAdapter & HalpbotAdapter> HalpbotCore addAdapters(T... adapters) {
        this.adapters.addAll(List.of(adapters));
        return this;
    }

    /**
     * Registers the {@link HalpbotAdapter} to the {@link JDABuilder}.
     *
     * @return Itself for chaining
     */
    public HalpbotCore registerAdapters() {
        this.adapters.forEach(this.jdaBuilder::addEventListeners);
        return this;
    }

    public JDA build() throws LoginException {
        jda = this.jdaBuilder.build();

    }
}
