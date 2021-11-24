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

import org.dockbox.hartshorn.persistence.exceptions.EmptyEntryException;
import org.dockbox.hartshorn.persistence.exceptions.IdentifierMismatchException;
import org.dockbox.hartshorn.persistence.table.ColumnIdentifier;
import org.dockbox.hartshorn.persistence.table.Merge;
import org.dockbox.hartshorn.persistence.table.Table;
import org.dockbox.hartshorn.persistence.table.TableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nz.pumbas.halpbot.sql.SQLDriver;
import nz.pumbas.halpbot.sql.SQLUtils;
import nz.pumbas.halpbot.sql.table.column.SimpleColumnIdentifier;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.context.LateInit;

//TODO: Replace with HH persistent service
public class SQLPermissionManager implements PermissionManager, LateInit
{
    private static final ColumnIdentifier<String> PERMISSION = new SimpleColumnIdentifier<>("permission", String.class);
    private static final ColumnIdentifier<Long> USER_ID      = new SimpleColumnIdentifier<>("userId", Long.class);
    private static final ColumnIdentifier<Integer> PERMISSION_ID = new SimpleColumnIdentifier<>("permissionId", Integer.class);

    private Table permissions;
    private Table joinedUserPermissions;

    private SQLDriver driver;

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        this.driver = SQLDriver.of("halpbotcore", this::populateDatabaseWithDefaultPermissions);
        this.driver.onLoad(this::cacheData);
    }

    private void cacheData(Connection connection) throws SQLException {
        ResultSet permissionsRS = this.driver.executeQuery(connection, "SELECT * FROM permissions");
        this.permissions = SQLUtils.asTable(permissionsRS, PERMISSION_ID, PERMISSION);

        ResultSet userPermissionsRS = this.driver.executeQuery(connection, "SELECT * FROM userPermissions");
        Table userPermissions = SQLUtils.asTable(userPermissionsRS, USER_ID, PERMISSION_ID);

        try {
            this.joinedUserPermissions = userPermissions.join(this.permissions, PERMISSION_ID, Merge.PREFER_ORIGINAL,
                    false);
        } catch (IdentifierMismatchException | EmptyEntryException e) {
            ErrorManager.handle(e);
        }
    }

    private void populateDatabaseWithDefaultPermissions(SQLDriver driver) throws SQLException {
        final String createPermissionTableSQL = "create table permissions ( permissionId INTEGER not null constraint " +
            "permissions_pk primary key autoincrement, permission TEXT ); create unique index permissions_id_uindex " +
            "on permissions (permissionId); create unique index permissions_permission_uindex on permissions (permission);";

        final String createUserPermissionTableSQL = "create table userPermissions ( userId INTEGER not null, " +
            "permissionId INTEGER not null, constraint userPermissions_pk primary key (userId, permissionId) );";

        try (Connection connection = driver.createConnection()) {
            driver.executeUpdate(connection, createPermissionTableSQL);
            driver.executeUpdate(connection, createUserPermissionTableSQL);

            this.insertPermissionToDatabase(
                driver, List.of(
                    HalpbotPermissions.BOT_OWNER,
                    HalpbotPermissions.BOT_OWNER,
                    HalpbotPermissions.ADMIN,
                    HalpbotPermissions.GIVE_PERMISSIONS));
        }
    }

    /**
     * Returns true if the {@link String permission} is registered in the database.
     *
     * @param permission
     *      The permission to check if registered in the database
     *
     * @return If the permission is registered
     */
    @Override
    public boolean isPermission(String permission) {
        return 0 != this.permissions.where(PERMISSION, permission.toLowerCase(Locale.ROOT)).count();
    }

    /**
     * Gives the user the specified permission. If the permission does not already exist in the database, it will be
     * automatically added by calling {@link PermissionManager#createPermissions(String...)}.
     *
     * @param userId
     *      The id of the user to give the permission to
     * @param permission
     *      The {@link String permission} to give the user
     */
    @Override
    public void givePermission(long userId, String permission) {
        permission = permission.toLowerCase(Locale.ROOT);

        if (!this.isPermission(permission))
            this.createPermissions(permission);
        int permissionId = this.permissions.where(PERMISSION, permission)
            .first().get()
            .value(PERMISSION_ID).get();

        try (Connection connection = this.driver.createConnection()) {
            this.driver.executeUpdate(connection, "INSERT INTO userPermissions (userId, permissionId) VALUES (?, ?)",
                userId, permissionId);
        }
        catch (SQLException e) {
            ErrorManager.handle(e);
        }
        TableRow newRow = new TableRow();
        newRow.add(USER_ID, userId)
            .add(PERMISSION_ID, permissionId)
            .add(PERMISSION, permission);
        try {
            this.joinedUserPermissions.addRow(newRow);
        } catch (IdentifierMismatchException e) {
            ErrorManager.handle(e);
        }
    }

    /**
     * Adds the following permissions to the database.
     * <p>
     * Note: The added permission won't be available until the
     * database refreshes and the newly created permission is cached.
     *
     * @param permissions
     *      The {@link List} of permissions to add to the database
     */
    @Override
    public void createPermissions(List<String> permissions) {
        this.insertPermissionToDatabase(this.driver, permissions);
    }

    private void insertPermissionToDatabase(SQLDriver driver, List<String> permissions) {
        final String insertPermissionSQL = "INSERT INTO permissions (permission) VALUES (?)";
        try (Connection connection = driver.createConnection();
             PreparedStatement statement = driver.createStatement(connection, insertPermissionSQL)) {

            for (String permission : permissions) {
                statement.setString(1, permission.toLowerCase(Locale.ROOT));
                statement.addBatch();
            }
            statement.executeBatch();
        }
        catch (SQLException e) {
            ErrorManager.handle(e);
        }
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
     *     The id of the user to check for the permissions
     * @param permissions
     *     The permissions to check that the user has
     *
     * @return If the user has the specified permissions
     */
    @Override
    public boolean hasPermissions(long userId, List<String> permissions) {
        List<String> userPermissions = this.getPermissions(userId);
        if (userPermissions.contains(HalpbotPermissions.BOT_OWNER))
            return true;

        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                boolean hasPermissionGroup = false;
                String permissionGroup = permission.substring(0, permission.lastIndexOf(".") + 1) + '*';
                while (-1 != permissionGroup.lastIndexOf(".", permissionGroup.length() -2))
                {
                    if (userPermissions.contains(permissionGroup)) {
                        hasPermissionGroup = true;
                        break;
                    }
                    permissionGroup = permissionGroup.substring(0,
                        permissionGroup.lastIndexOf(".", permissionGroup.length() - 3) + 1) + '*';
                }
                if (!hasPermissionGroup) return false;
            }
        }
        return true;
    }

    /**
     * Returns the {@link List} of permissions that the specified user has.
     *
     * @param userId
     *      The id of the user to get the permissions for
     *
     * @return The {@link List} of permissions that the user has
     */
    @Override
    public List<String> getPermissions(long userId) {
        List<String> permissions = new ArrayList<>();

        this.joinedUserPermissions
            .where(USER_ID, userId)
            .forEach(row -> row.value(PERMISSION)
                .present(permissions::add));

        return permissions;
    }

    /**
     * Returns a {@link List} containing all the different permissions.
     *
     * @return All the different permissions
     */
    @Override
    public List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        this.permissions.forEach(row -> row.value(PERMISSION)
            .present(permissions::add));
        return permissions;
    }
}
