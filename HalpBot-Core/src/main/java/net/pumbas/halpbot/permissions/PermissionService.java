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

package net.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.pumbas.halpbot.common.CoreCarrier;
import net.pumbas.halpbot.configurations.BotConfiguration;
import net.pumbas.halpbot.permissions.repositories.GuildPermission;
import net.pumbas.halpbot.permissions.repositories.GuildPermissionId;

import org.dockbox.hartshorn.context.ContextCarrier;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PermissionService extends ContextCarrier, CoreCarrier {

    /**
     * Determines if the member is the owner of this bot.
     *
     * @param member
     *     The member to check
     *
     * @return Whether the member is the owner of this bot
     */
    default boolean isOwner(final Member member) {
        return this.halpbotCore().ownerId() == member.getIdLong();
    }

    /**
     * @return If role binding has been enabled. This can be determined from {@link BotConfiguration#useRoleBinding()}
     */
    boolean useRoleBinding();

    /**
     * Any initialisation that needs to be done after all the permission suppliers have been registered should be done
     * here.
     */
    default void initialise() {}

    /**
     * Registers all the permission suppliers located within type. It automatically checks if the permission supplier
     * methods has the parameters {@code Guild} and {@code Member} respectively and return a boolean. If it doesn't then
     * it logs a warning and ignores the method.
     *
     * @param typeContext
     *     The type to scan for permission suppliers
     * @param <T>
     *     The type being scanned for permission suppliers
     */
    @SuppressWarnings("unchecked")
    default <T> void registerPermissionSuppliers(final TypeContext<T> typeContext) {
        final T instance = this.applicationContext().get(typeContext);
        final List<? extends MethodContext<?, T>> permissionSuppliers = typeContext.methods(PermissionSupplier.class);

        int validPermissionSuppliers = 0;

        // First validate the permission suppliers
        for (final MethodContext<?, ?> permissionSupplier : permissionSuppliers) {
            final List<TypeContext<?>> parameters = permissionSupplier.parameterTypes();
            if (parameters.size() != 2 || !parameters.get(0).is(Guild.class) || !parameters.get(1).is(Member.class)) {
                this.applicationContext().log()
                    .warn("The permission supplier %s must only have the parameters %s and %s"
                        .formatted(permissionSupplier.qualifiedName(),
                            Guild.class.getCanonicalName(),
                            Member.class.getCanonicalName()));
                continue;
            }

            if (!permissionSupplier.returnType().is(boolean.class)) {
                this.applicationContext().log().warn("The permission supplier %s must return a boolean"
                    .formatted(permissionSupplier.qualifiedName()));
                continue;
            }

            validPermissionSuppliers++;
            final String permission = permissionSupplier.annotation(PermissionSupplier.class).get().value();
            this.registerPermissionSupplier(instance, permission, (MethodContext<Boolean, T>) permissionSupplier);
        }
        this.applicationContext().log().info("Registered %d permission suppliers in %s"
            .formatted(validPermissionSuppliers, typeContext.qualifiedName()));
    }

    /**
     * Registers a permission supplier. This method should be annotated with {@link PermissionSupplier}, contain the
     * parameters {@code Guild} and {@code Member} respectively and return a boolean. This method does not check these
     * conditions as they should've already been checked prior to calling this method.
     *
     * @param instance
     *     The instance of which the method is within
     * @param permission
     *     The permission being supplied
     * @param predicate
     *     The permission supplier method
     * @param <T>
     *     The type of the instance
     *
     * @see PermissionService#registerPermissionSuppliers(TypeContext)
     */
    <T> void registerPermissionSupplier(T instance, String permission, MethodContext<Boolean, T> predicate);

    /**
     * Updates or saves the guild permission as appropriate and returns it again as a persistent entity. If role binding
     * is disabled this does nothing.
     *
     * @param guildPermission
     *     The guild permission to update or save
     *
     * @return The guild permission as a persistent entity
     */
    GuildPermission updateOrSave(GuildPermission guildPermission);

    /**
     * Updates the saved guild permission and returns it again as a persistent entity. If role binding is disabled this
     * does nothing.
     *
     * @param guildPermission
     *     The guild permission to update
     *
     * @return The guild permission as a persistent entity
     */
    GuildPermission update(GuildPermission guildPermission);

    /**
     * Saves the guild permission and returns it again as a persistent entity. If role binding is disabled this does
     * nothing.
     *
     * @param guildPermission
     *     The guild permission to save
     *
     * @return The guild permission as a persistent entity
     */
    GuildPermission save(GuildPermission guildPermission);

    /**
     * Deletes the stored guild permission. If role binding is disabled then this does nothing.
     *
     * @param guildPermission
     *     The guild permission to delete
     */
    void delete(GuildPermission guildPermission);

    /**
     * Returns true if the custom permission is registered.
     *
     * @param permission
     *     The permission to check. This can be either a bindable permission or a supplied permission
     *
     * @return If the permission is registered
     */
    boolean isRegistered(String permission);

    /**
     * Determines if the specified role is bound to a permission within the guild. If role binding is disabled this will
     * always return false.
     *
     * @param guildId
     *     The id of the guild to see if the role is bound to a permission within
     * @param roleId
     *     The id of the role to determine if there's a permission bound to
     *
     * @return If the role is bound to a permission within the guild
     */
    boolean isRoleBound(long guildId, long roleId);

    /**
     * Determines if the specified permission is bound to a role within the guild. If role binding is disabled this will
     * always return false.
     *
     * @param guildId
     *     The id of the guild to see if the permission is bound within
     * @param permission
     *     The permission to check if bound within the guild
     *
     * @return If the permission is bound to a role within the guild
     */
    boolean isPermissionBound(long guildId, String permission);

    /**
     * Adds the custom permissions. This should include both role bindable permissions and permission supplied
     * permissions.
     *
     * @param permissions
     *     The permissions to add to the service
     *
     * @see PermissionService#addPermissions(Set)
     */
    default void addPermissions(final String... permissions) {
        this.addPermissions(Set.of(permissions));
    }

    /**
     * Adds the custom permissions. This should include both role bindable permissions and permission supplied
     * permissions.
     *
     * @param permissions
     *     The permissions to add to the service
     *
     * @see PermissionService#addPermissions(String...)
     */
    void addPermissions(Set<String> permissions);

    /**
     * Determines whether the member has ALL the specified permissions within the guild or not. This will first check if
     * there's a permission supplier for the permission otherwise it will check if they have the permissions bound role.
     * If role binding has been disabled, it will instantly return false rather than check the members roles. This
     * returns a {@link CompletableFuture} as the {@link Member} object may need to be fetched from Discord if it's not
     * cached.
     *
     * @param guild
     *     The guild to check the users permissions within
     * @param user
     *     The user to check the permissions against
     * @param permissions
     *     The permissions to check that the user has all of
     *
     * @return A {@link CompletableFuture} containing whether the user has ALL the permissions
     * @see PermissionService#hasPermission(Guild, Member, String)
     * @see PermissionService#hasPermissions(Guild, Member, Set)
     */
    default CompletableFuture<Boolean> hasPermissions(final Guild guild, final User user, final Set<String> permissions) {
        return guild.retrieveMember(user).submit()
            .thenApply((member) -> this.hasPermissions(guild, member, permissions));
    }

    /**
     * Determines whether the member has ALL the specified permissions within the guild or not. This will first check if
     * there's a permission supplier for the permission otherwise it will check if they have the permissions bound role.
     * If role binding has been disabled, it will instantly return false rather than check the members roles.
     *
     * @param guild
     *     The guild to check the members permissions within
     * @param member
     *     The member to check the permissions against
     * @param permissions
     *     The permissions to check that the member has all of
     *
     * @return Whether the member has all the specified permissions within the guild
     * @see PermissionService#hasPermissions(Guild, User, Set)
     * @see PermissionService#hasPermission(Guild, Member, String)
     */
    default boolean hasPermissions(final Guild guild, final Member member, final Set<String> permissions) {
        for (final String permission : permissions) {
            if (!this.hasPermission(guild, member, permission))
                return false;
        }
        return true;
    }

    /**
     * Determines whether the member has the specified permission within the guild or not. This will first check if
     * there's a permission supplier for the permission otherwise it will check if they have the permissions bound role.
     * If role binding has been disabled, it will instantly return false rather than check the members roles.
     *
     * @param guild
     *     The guild to check the members permissions within
     * @param member
     *     The member to check the permission against
     * @param permission
     *     The permission to check that the member has
     *
     * @return Whether the member has the permissions within the guild
     * @see PermissionService#hasPermissions(Guild, User, Set)
     * @see PermissionService#hasPermissions(Guild, Member, Set)
     */
    boolean hasPermission(Guild guild, Member member, String permission);

    /**
     * Retrieves an {@link Result} containing the bound role for the specified permission in the guild.
     *
     * @param guild
     *     The guild to find the bound permission role in
     * @param permission
     *     The permission to find the bound role for
     *
     * @return An {@link Result} containing the bound role
     * @see PermissionService#findById(GuildPermissionId)
     */
    default Result<Role> guildRole(final Guild guild, final String permission) {
        return this.findById(new GuildPermissionId(guild.getIdLong(), permission))
            .map((gp) -> guild.getRoleById(gp.roleId()));

    }

    /**
     * Finds a stored guild permission by the {@link GuildPermissionId id}. If there is no role bound to the particular
     * permission in that guild then an empty Result will be returned. If role binding is disabled it will always return
     * an empty Result.
     *
     * @param id
     *     The {@link GuildPermissionId} containing the guild and permission that you're looking for
     *
     * @return An {@link Result} containing the guild permission it exists.
     */
    Result<GuildPermission> findById(GuildPermissionId id);

    /**
     * Retrieves the role bindable permissions that the specified member has in a particular guild. This is achieved by
     * retreiving all the members roles and then matching those roles to bound permissions. This returns a
     * {@link CompletableFuture} as the {@link Member} object may need to be fetched from Discord if it's not cached.
     *
     * @param guild
     *     The guild to check the users permissions within
     * @param user
     *     The user to get the permissions of
     *
     * @return A {@link CompletableFuture} of the users bindable permissions within the specified guild
     * @see PermissionService#permissions(long, Member)
     */
    default CompletableFuture<Set<String>> permissions(final Guild guild, final User user) {
        final long guildId = guild.getIdLong();
        return guild.retrieveMember(user).submit()
            .thenApply(member -> this.permissions(guildId, member));
    }

    /**
     * Retrieves the role bindable permissions that the specified member has in a particular guild. This is achieved by
     * retreiving all the members roles and then matching those roles to bound permissions. If role binding has been
     * disabled then this will always return an empty set.
     *
     * @param guildId
     *     The id of the guild to check the members permissions in
     * @param member
     *     The member to check for permissions
     *
     * @return A {@link Set} of the members bindable permissions in the specified guild
     * @see PermissionService#permissions(Guild, User)
     */
    Set<String> permissions(long guildId, Member member);

    /**
     * @return An unmodifiable {@link Set} containing all the registered custom permissions. These can either be
     *     permission suppliers or role bound permissions
     */
    Set<String> permissions();

    /**
     * @return A {@link Set} containing all the registered
     *     <a href="https://github.com/pumbas600/Halpbot/wiki/Permissions#role-binding">role binding</a>
     *     permissions
     */
    Set<String> rolePermissions();

    /**
     * Retrieves a {@link Map} of the role bindable permissions and their respective role id for the guild specified. If
     * the permission is unbound, then the role id will be null. If role binding has been disabled then it will always
     * return an empty map.
     *
     * @param guild
     *     The guild to find the role bindings for
     *
     * @return A map containing the role ids bound to each permission in this guild
     */
    Map<String, Long> roleBindings(Guild guild);
}
