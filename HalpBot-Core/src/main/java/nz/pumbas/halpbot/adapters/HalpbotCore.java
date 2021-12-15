package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.boot.HartshornExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.jetbrains.annotations.Nullable;

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
import nz.pumbas.halpbot.commands.exceptions.UndefinedActivatorException;
import nz.pumbas.halpbot.common.annotations.Bot;
import nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class HalpbotCore implements ContextCarrier
{
    @Getter private long ownerId = -1;

    @Inject
    @Getter private ApplicationContext applicationContext;
    @Getter private DisplayConfiguration displayConfiguration = new SimpleDisplayConfiguration();

    @Nullable private JDA jda;

    private final List<HalpbotAdapter> adapters = new ArrayList<>();
    private final Map<Long, UserCooldowns> userCooldownsMap = new ConcurrentHashMap<>();

    private final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    private final PermissionManager permissionManager = HalpbotUtils.context().get(PermissionManager.class);

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

    private void onCreation(JDA jda) throws UndefinedActivatorException {
        if (!this.applicationContext.hasActivator(Bot.class)) {
            throw new UndefinedActivatorException("The @Bot activator must be present on the main class");
        }
        Bot bot = this.applicationContext.activator(Bot.class);

        this.displayConfiguration = this.applicationContext.get(bot.displayConfiguration());
        this.adapters.forEach(adapter -> adapter.onCreation(jda));
    }

    public JDA build(JDABuilder jdaBuilder) throws ApplicationException {
        this.adapters.forEach(jdaBuilder::addEventListeners);

        try {
            this.jda = jdaBuilder.build();
            this.onCreation(this.jda);
        } catch (LoginException | UndefinedActivatorException e) {
            throw new ApplicationException(e);
        }

        this.concurrentManager.scheduleRegularly(20, 20, TimeUnit.MINUTES, this::clearEmptyCooldowns);
        return this.jda;
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
                        this.displayConfiguration().displayTemporary(event, remainingTimeEmbed, -1))
                    .absent(() -> {
                        if (userCooldowns.canSendCooldownMessage()) {
                            this.displayConfiguration().displayTemporary(event, remainingTimeEmbed, 10);
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
