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

package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import nz.pumbas.halpbot.permissions.repositories.GuildPermission;

public interface PermissionService
{
    /**
     * Returns true if the {@link String permission} is registered in the database.
     *
     * @param permission
     *      The permission to check if registered in the database
     *
     * @return If the permission is registered
     */
    boolean isPermission(String permission);

    boolean isPermission(long guildId, long roleId);

    boolean permissionExists(long guildId, String permission);

    default void updateOrSave(long guildId, String permission, long roleId) {
        this.updateOrSave(new GuildPermission(guildId, permission, roleId));
    }

    void updateOrSave(GuildPermission guildPermission);

    /**
     * Adds the following permissions to the database.
     *
     * @param permissions
     *      The {@link List} of permissions to add to the database
     */
    void addPermissions(Set<String> permissions);

    /** Adds the following permissions to the database.
     * <p>
     * Note: The added permission won't be available until the
     * database refreshes and the newly created permission is cached.
     *
     * @param permissions
     *      The permissions to add to the database
     */
    default void addPermissions(String... permissions) {
        this.addPermissions(Set.of(permissions));
    }

    /**
     * Returns true of the user has all the following permissions. Note: If the user has the
     * {@link HalpbotPermissions#BOT_OWNER} permission, then this will always return true as the owner has all permissions.
     * If they don't have the exact permission, it will then start looking for permission groups which include the
     * desired permission. For example, if you're checking if a user has the
     * {@code halpbot.admin.give.permission} permission and you don't have that exact permission, it will then check
     * if you have the {@code halpbot.admin.give.*} permission, then {@code halpbot.admin.*}, etc.
     *
     *
     * @param userId
     *      The id of the user to check for the permissions
     * @param permissions
     *      The permissions to check that the user has
     *
     * @return If the user has the specified permissions
     */
    default boolean hasPermission(long userId, Set<String> permissions) {
        for (String permission : permissions) {
            if (!this.hasPermission(userId, permission))
                return false;
        }
        return true;
    }

    default boolean hasPermissions(Guild guild, Member member, Set<String> permissions) {
        for (String permission : permissions) {
            if (!this.hasPermission(guild, member, permission))
                return false;
        }
        return true;
    }

    boolean hasPermission(Guild guild, Member member, String permission);

    default CompletableFuture<Boolean> hasPermissions(Guild guild, User user, Set<String> permissions) {
        return guild.retrieveMember(user).submit()
                .thenApply((member) -> this.hasPermissions(guild, member, permissions));
    }

    /**
     * Returns true of the user has all the following permissions. Note: If the user has the
     * {@link HalpbotPermissions#BOT_OWNER} permission, then this will always return true as the owner has all permissions.
     * If they don't have the exact permission, it will then start looking for permission groups which include the
     * desired permission. For example, if you're checking if a user has the
     * {@code halpbot.admin.give.permission} permission and you don't have that exact permission, it will then check
     * if you have the {@code halpbot.admin.give.*} permission, then {@code halpbot.admin.*}, etc.
     *
     * @return If the user has the specified permissions
     */
    boolean hasPermission(long userId, String permission);

    default boolean hasPermission(User user, String permissions) {
        return this.hasPermission(user.getIdLong(), permissions);
    }

    Set<String> permissions(long guildId, Member member);

    default CompletableFuture<Set<String>> permissions(Guild guild, User user) {
        final long guildId = guild.getIdLong();
        return guild.retrieveMember(user).submit()
                .thenApply(member -> this.permissions(guildId, member));
    }

    /**
     * Returns a {@link List} containing all the different permissions.
     *
     * @return All the different permissions
     */
    Set<String> permissions();
}
