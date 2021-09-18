package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.context.ContextHolder;

public class HalpbotCore implements ContextHolder
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
        this.adapters.forEach(adapter -> adapter.accept(jda));
        return jda;
    }

    /**
     * Retrieves the instance of the specified {@link Class implementation}. If there isn't already an implementation
     * for that class, then it tries to create one, assuming it has a constructor that takes no parameters. If the
     * specified {@link Class}, or a bound implementation if present is abstract or an interface, null is returned.
     *
     * @param implementation
     *     The {@link Class implementation} of the instance to retrieve
     *
     * @return The instance, or null if there isn't one registered.
     */
    @Override
    public <T> T get(final Class<T> implementation) {
        return this.getSafely(implementation).orNull();
    }

    /**
     * Retrieves an {@link Exceptional} of the instance of the specified {@link Class contract}.
     *
     * @param contract
     *     The {@link Class contract} of the instance to retrieve
     *
     * @return An {@link Exceptional} of the instance, or {@link Exceptional#empty()} if there isn't one registered.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Exceptional<T> getSafely(final Class<T> contract) {
        return Exceptional.of(
            this.adapters
            .stream()
            .filter(adapter -> adapter.getClass().isAssignableFrom(contract))
            .findFirst()
            .map(adapter -> (T)adapter));
    }
}
