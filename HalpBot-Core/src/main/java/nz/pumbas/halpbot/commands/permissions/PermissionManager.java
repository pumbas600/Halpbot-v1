package nz.pumbas.halpbot.commands.permissions;

import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface PermissionManager
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

    /**
     * Gives the user the specified permission. If the permission does not already exist in the database, it will be
     * automatically added by calling {@link PermissionManager#createPermissions(String...)}.
     *
     * @param userId
     *      The id of the user to give the permission to
     * @param permission
     *      The {@link String permission} to give the user
     */
    void givePermission(long userId, String permission);

    /**
     * Gives the user the specified permission. If the permission does not already exist in the database, it will be
     * automatically added by calling {@link PermissionManager#createPermissions(String...)}.
     *
     * @param user
     *      The {@link User} to give the permission to
     * @param permission
     *      The {@link String permission} to give the user
     */
    default void givePermission(User user, String permission) {
        this.givePermission(user.getIdLong(), permission);
    }

    /**
     * Adds the following permissions to the database.
     *
     * @param permissions
     *      The {@link List} of permissions to add to the database
     */
    void createPermissions(List<String> permissions);

    /** Adds the following permissions to the database.
     * <p>
     * Note: The added permission won't be available until the
     * database refreshes and the newly created permission is cached.
     *
     * @param permissions
     *      The permissions to add to the database
     */
    default void createPermissions(String... permissions) {
        this.createPermissions(List.of(permissions));
    }

    /**
     * Returns true of the user has all the following permissions. Note: If the user has the
     * {@link HalpbotPermissions#BOT_OWNER} permission, then this will always return true as the owner has all permissions.
     * If they don't have the exact permission, it will then start looking for permission groups which include the
     * desired permission. For example, if you're checking if a user has the
     * {@code halpbot.admin.give.permission} permission and you don't have that exact permission, it will then check
     * if you have the {@code halpbot.admin.give.*} permission, then {@code halpbot.admin.*}, etc.
     *
     * @param userId
     *      The id of the user to check for the permissions
     * @param permissions
     *      The permissions to check that the user has
     *
     * @return If the user has the specified permissions
     */
    boolean hasPermissions(long userId, String... permissions);

    /**
     * Returns true of the user has all the following permissions. Note: If the user has the
     * {@link HalpbotPermissions#BOT_OWNER} permission, then this will always return true as the owner has all permissions.
     *
     * @param user
     *      The {@link User} to check for the permissions
     * @param permissions
     *      The permissions to check that the user has
     *
     * @return If the user has the specified permissions
     */
    default boolean hasPermissions(User user, String... permissions) {
        return this.hasPermissions(user.getIdLong(), permissions);
    }

    /**
     * Returns the {@link List} of permissions that the specified user has.
     *
     * @param userId
     *      The id of the user to get the permissions for
     *
     * @return The {@link List} of permissions that the user has
     */
    List<String> getPermissions(long userId);

    /**
     * Returns the {@link List} of permissions that the specified user has.
     *
     * @param user
     *      The {@link User} to get the permissions for
     *
     * @return The {@link List} of permissions that the user has
     */
    default List<String> getPermissions(User user) {
        return this.getPermissions(user.getIdLong());
    }

    /**
     * Returns a {@link List} containing all the different permissions.
     *
     * @return All the different permissions
     */
    List<String> getAllPermissions();
}
