package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import lombok.Getter;
import nz.pumbas.halpbot.adapters.AbstractHalpbotAdapter;
import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class HalpbotCore implements ContextCarrier
{
    @Getter private long ownerId = -1;

    @Getter
    @Inject private ApplicationContext applicationContext;
    @Inject private PermissionService permissionService;

    @Getter private DisplayConfiguration displayConfiguration = new SimpleDisplayConfiguration();

    @Nullable private JDA jda;

    private final List<HalpbotAdapter> adapters = new ArrayList<>();

    private final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);

    /**
     * Adds the {@link AbstractHalpbotAdapter}s to the core. This will automatically register the adapters when you invoke
     * {@link HalpbotCore#registerAdapter(HalpbotAdapter)}. Note that the Halpbot adapters will also need to extend
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
    public final <T extends HalpbotAdapter> HalpbotCore addAdapters(T... adapters) {
        this.adapters.addAll(List.of(adapters));
        return this;
    }

    /**
     * Sets the id of the owner for this bot. This automatically assigns the user the
     * {@link HalpbotPermissions#BOT_OWNER} permission if they don't already have it in the database.
     *
     * @param ownerId
     *      The {@link Long id} of the owner
     *
     * @return Itself for chaining
     */
    public HalpbotCore setOwner(long ownerId) {
        this.ownerId = ownerId;
//        if (!this.permissionManager.hasPermissions(ownerId, HalpbotPermissions.BOT_OWNER))
//            this.permissionManager.givePermission(ownerId, HalpbotPermissions.BOT_OWNER);
        return this;
    }

    private void onCreation(JDA jda) throws ApplicationException {
        BotConfiguration config = this.applicationContext.get(BotConfiguration.class);

        this.determineDisplayConfiguration(config);
        if (config.ownerId() == -1)
            throw new ApplicationException("You must specify the id of the bot owner in bot-config.properties");

        this.setOwner(config.ownerId());
        this.permissionService.validateSetup();
        this.adapters.forEach(adapter -> adapter.onCreation(jda));
    }

    private void determineDisplayConfiguration(BotConfiguration config) {
        TypeContext<?> typeContext = TypeContext.lookup(config.displayConfiguration());
        if (!typeContext.childOf(DisplayConfiguration.class)) {
            this.applicationContext.log()
                    .warn("The display configuration %s specified in bot-config.properties must implement DisplayConfiguration"
                            .formatted(config.displayConfiguration()));
            this.applicationContext.log().warn("Falling back to %s display configuration"
                    .formatted(SimpleDisplayConfiguration.class.getCanonicalName()));
            this.displayConfiguration = new SimpleDisplayConfiguration();
        }
        else {
            this.displayConfiguration = (DisplayConfiguration) this.applicationContext.get(typeContext);
        }
    }

    public JDA build(JDABuilder jdaBuilder) throws ApplicationException {
        jdaBuilder.setEventManager(new AnnotatedEventManager());
        this.adapters.forEach(jdaBuilder::addEventListeners);

        try {
            this.jda = jdaBuilder.build();
            this.onCreation(this.jda);
        } catch (LoginException e) {
            ExceptionHandler.unchecked(e);
        }

        return this.jda;
    }

    public <T extends HalpbotAdapter> HalpbotCore registerAdapter(T adapter) {
        this.adapters.add(adapter);
        return this;
    }

    @Provider
    public JDA jda() {
        if (this.jda == null)
            ExceptionHandler.unchecked(
                    new ApplicationException("You are trying to access the JDA instance before it has been created"));
        return this.jda;
    }

    @Nullable
    public <T> T get(final Class<T> implementation) {
        return this.get(TypeContext.of(implementation));
    }

    @Nullable
    public <T> T get(final TypeContext<T> implementation) {
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
    @SuppressWarnings("unchecked")
    public <T> Exceptional<T> getSafely(final TypeContext<T> contract) {
        return Exceptional.of(
            this.adapters
            .stream()
            .filter(adapter -> TypeContext.of(adapter).childOf(contract))
            .findFirst()
            .map(adapter -> (T)adapter));
    }
}
