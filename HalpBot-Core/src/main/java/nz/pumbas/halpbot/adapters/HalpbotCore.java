package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import nz.pumbas.halpbot.commands.DiscordString;
import nz.pumbas.halpbot.commands.cooldowns.Cooldown;
import nz.pumbas.halpbot.commands.cooldowns.UserCooldowns;
import nz.pumbas.halpbot.commands.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.commands.permissions.PermissionManager;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.context.ContextHolder;

public class HalpbotCore implements ContextHolder
{
    private static JDA jda;
    private long ownerId = -1;
    private final JDABuilder jdaBuilder;

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

    public JDA build() throws LoginException {
        jda = this.jdaBuilder.build();
        this.adapters.forEach(adapter -> adapter.accept(jda));
        this.concurrentManager.scheduleRegularly(20, 20, TimeUnit.MINUTES, this::clearEmptyCooldowns);
        return jda;
    }

    /**
     * Uses the specified {@link DisplayConfiguration} to display the message.
     *
     * @param event
     *      The {@link GenericMessageEvent}
     * @param message
     *      The message to display
     */
    public void displayMessage(GenericMessageEvent event, Object message) {
        if (message instanceof MessageEmbed)
            this.displayConfiguration.display(event, (MessageEmbed) message);
        else {
            String displayMessage = message instanceof DiscordString
                ? ((DiscordString) message).toDiscordString()
                : message.toString();
            this.displayConfiguration.display(event, displayMessage);
        }
    }

    /**
     * Uses the specified {@link DisplayConfiguration} to display the message.
     *
     * @param interaction
     *      The {@link Interaction}
     * @param message
     *      The message to display
     */
    public void displayMessage(Interaction interaction, Object message) {
        if (message instanceof MessageEmbed)
            this.displayConfiguration.display(interaction, (MessageEmbed) message);
        else {
            String displayMessage = message instanceof DiscordString
                ? ((DiscordString) message).toDiscordString()
                : message.toString();
            this.displayConfiguration.display(interaction, displayMessage);
        }
    }

    public void addCooldown(long userId, long actionId, Cooldown cooldown) {
       UserCooldowns userCooldowns;
        if (!this.userCooldownsMap.containsKey(userId)) {
            userCooldowns = new UserCooldowns();
            this.userCooldownsMap.put(userId, userCooldowns);
        }
        else userCooldowns = this.userCooldownsMap.get(userId);

        userCooldowns.addCooldown(actionId, cooldown);
    }

    public boolean hasCooldown(long userId, long actionId) {
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
    public boolean hasCooldown(GenericMessageEvent event, long userId, long actionId) {
        if (this.userCooldownsMap.containsKey(userId)) {
            UserCooldowns userCooldowns = this.userCooldownsMap.get(userId);
            if (userCooldowns.hasCooldownFor(actionId)) {
                if (userCooldowns.canSendCooldownMessage()) {
                    Cooldown cooldown = userCooldowns.getCooldownFor(actionId);
                    event.getChannel().sendMessageEmbeds(cooldown.getRemainingTimeEmbed())
                        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                }
                return true;
            }
        }
        return false;
    }

    public void clearEmptyCooldowns() {
       this.userCooldownsMap.entrySet()
           .removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * @return The id of the owner for this bot, or -1 if no owner has been specified
     */
    public long getOwnerId() {
        return this.ownerId;
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
