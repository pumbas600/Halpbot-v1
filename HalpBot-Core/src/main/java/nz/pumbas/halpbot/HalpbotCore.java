/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.boot.ExceptionHandler;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

@Service
public class HalpbotCore implements ContextCarrier
{
    @Getter private long ownerId = -1;

    @Getter
    @Inject private ApplicationContext applicationContext;
    @Inject private PermissionService permissionService;

    @Getter private DisplayConfiguration displayConfiguration = new SimpleDisplayConfiguration();
    @Getter private final ScheduledExecutorService threadpool;

    @Nullable private JDA jda;

    private final List<HalpbotAdapter> adapters = new ArrayList<>();
    private final List<EventListener> eventListeners = new ArrayList<>();

    public HalpbotCore() {
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
    }

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
        return this;
    }

    public void onCreation(JDA jda) throws ApplicationException {
        BotConfiguration config = this.applicationContext.get(BotConfiguration.class);

        this.determineDisplayConfiguration(config);
        if (config.ownerId() == -1)
            throw new ApplicationException("You must specify the id of the bot owner in bot-config.properties");

        this.setOwner(config.ownerId());
        this.permissionService.initialise();
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
        this.adapters.forEach((adapter) -> adapter.initialise(jdaBuilder));
        this.adapters.forEach(jdaBuilder::addEventListeners);

        // Prevent any event listeners being automatically registered twice
        jdaBuilder.removeEventListeners(this.eventListeners.toArray());
        this.eventListeners.forEach(jdaBuilder::addEventListeners);

        this.eventListeners.clear(); // Free up the memory space, we don't to store this anymore

        try {
            this.jda = jdaBuilder.build().awaitReady();
            this.onCreation(this.jda);
        } catch (LoginException | InterruptedException e) {
            ExceptionHandler.unchecked(e);
        }

        return this.jda;
    }

    public <T extends HalpbotAdapter> HalpbotCore registerAdapter(T adapter) {
        this.adapters.add(adapter);
        return this;
    }

    public void registerEventListener(EventListener eventListener) {
        this.eventListeners.add(eventListener);
    }

    @Provider
    public JDA jda() {
        if (this.jda == null)
            ExceptionHandler.unchecked(
                    new ApplicationException("You are trying to access the JDA instance before it has been created"));
        return this.jda;
    }
}
