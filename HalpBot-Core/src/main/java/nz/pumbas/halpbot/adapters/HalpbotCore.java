package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import lombok.Getter;
import nz.pumbas.halpbot.actions.cooldowns.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.UserCooldowns;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class HalpbotCore implements ContextCarrier
{
    private static JDA jda;
    private long ownerId = -1;
    private final JDABuilder jdaBuilder;

    @Inject
    @Getter private ApplicationContext applicationContext;

    private final List<HalpbotAdapter> adapters = new ArrayList<>();
    private final Map<Long, UserCooldowns> userCooldownsMap = new ConcurrentHashMap<>();

    private final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    private final PermissionManager permissionManager = HalpbotUtils.context().get(PermissionManager.class);
    private DisplayConfiguration displayConfiguration = new SimpleDisplayConfiguration();

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
    public final <T extends HalpbotAdapter> HalpbotCore addAdapters(T... adapters) {
        this.adapters.addAll(List.of(adapters));
        return this;
    }

    /**
     * Registers the {@link HalpbotAdapter} to the {@link JDABuilder}. Note that this also invokes
     * {@link HalpbotAdapter#setHalpBotCore(HalpbotCore)}.
     *
     * @return Itself for chaining
     */
    public HalpbotCore registerAdapters() {
        this.adapters.forEach(adapter -> {
            adapter.setHalpBotCore(this);
            this.jdaBuilder.addEventListeners(adapter);
        });
        return this;
    }

    /**
     * Specifies the display configuration that should be used when displaying messages in discord. if no
     * configuration is specified then {@link nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration} is used.
     *
     * @param displayConfiguration
     *      The display configuration to use
     *
     * @return Itself for chaining
     */
    public HalpbotCore displayConfiguration(DisplayConfiguration displayConfiguration) {
        this.displayConfiguration = displayConfiguration;
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
        if (!this.permissionManager.hasPermissions(ownerId, HalpbotPermissions.BOT_OWNER))
            this.permissionManager.givePermission(ownerId, HalpbotPermissions.BOT_OWNER);
        return this;
    }

    /**
     * Registers the objects with each of the adapters by calling their respective
     * {@link HalpbotAdapter#registerObjects(Object...)} methods.
     *
     * @param objects
     *      The objects to register
     *
     * @return Itself for chaining
     */
    public HalpbotCore register(Object... objects) {
        this.adapters.forEach(adapter -> adapter.registerObjects(objects));
        return this;
    }

    public JDA build() throws LoginException {
        jda = this.jdaBuilder.build();
        this.adapters.forEach(adapter -> adapter.accept(jda));
        this.concurrentManager.scheduleRegularly(20, 20, TimeUnit.MINUTES, this::clearEmptyCooldowns);
        return jda;
    }

    public void addCooldown(long userId, String actionId, Cooldown cooldown) {
       UserCooldowns userCooldowns;
        if (!this.userCooldownsMap.containsKey(userId)) {
            userCooldowns = new UserCooldowns();
            this.userCooldownsMap.put(userId, userCooldowns);
        }
        else userCooldowns = this.userCooldownsMap.get(userId);

        userCooldowns.addCooldown(actionId, cooldown);
    }

    public boolean hasCooldown(long userId, String actionId) {
        return this.userCooldownsMap.containsKey(userId)
            && this.userCooldownsMap.get(userId).hasCooldownFor(actionId);
    }

    /**
     * Check if the user has a cooldown for the specified action. If they do, it displays a cooldown message telling
     * the user how long they have to wait before they can do the action again. This message will automatically
     * delete after a short period of time. Note that this message has a cooldown too. If their 'cooldown message' is
     * cooling down, then nothing will be displayed, preventing users from spamming the cooldown message.
     *
     * @param event
     *      The event to send display the message with
     * @param userId
     *      The user id
     * @param actionId
     *      The action id
     *
     * @return If they have a cooldown.
     */
    public boolean hasCooldown(HalpbotEvent event, long userId, String actionId) {
        if (this.userCooldownsMap.containsKey(userId)) {
            UserCooldowns userCooldowns = this.userCooldownsMap.get(userId);
            if (userCooldowns.hasCooldownFor(actionId))
            {
                final MessageEmbed remainingTimeEmbed = userCooldowns.getCooldownFor(actionId)
                    .getRemainingTimeEmbed();

                event.safelyGetEvent(Interaction.class)
                    .present(interaction ->
                        this.getDisplayConfiguration().displayTemporary(event, remainingTimeEmbed, -1))
                    .absent(() -> {
                        if (userCooldowns.canSendCooldownMessage()) {
                            this.getDisplayConfiguration().displayTemporary(event, remainingTimeEmbed, 10);
                        }
                    });
                return true;
            }
        }
        return false;
    }

    public void clearEmptyCooldowns() {
       this.userCooldownsMap.entrySet()
           .removeIf(entry -> entry.getValue().isEmpty());
    }

    public DisplayConfiguration getDisplayConfiguration() {
        return this.displayConfiguration;
    }

    /**
     * @return The id of the owner for this bot, or -1 if no owner has been specified
     */
    public long getOwnerId() {
        return this.ownerId;
    }

    public <T extends HalpbotAdapter> T getAndRegister(TypeContext<T> adapterType) {
        Exceptional<T> registeredInstance = this.getSafely(adapterType);
        if (registeredInstance.present())
            return registeredInstance.get();
        T instance = this.applicationContext.get(adapterType);
        this.register(instance);
        return instance;
    }

    public <T> T get(final Class<T> implementation) {
        return this.get(TypeContext.of(implementation));
    }

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
