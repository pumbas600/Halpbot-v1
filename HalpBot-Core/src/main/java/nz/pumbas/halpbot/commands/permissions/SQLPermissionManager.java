package nz.pumbas.halpbot.commands.permissions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nz.pumbas.halpbot.sql.SQLDriver;
import nz.pumbas.halpbot.sql.SQLManager;
import nz.pumbas.halpbot.sql.SQLUtils;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.column.SimpleColumnIdentifier;
import nz.pumbas.halpbot.sql.table.exceptions.IdentifierMismatchException;
import nz.pumbas.halpbot.sql.table.exceptions.ValueMismatchException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.context.LateInit;

public class SQLPermissionManager implements PermissionManager, LateInit
{
    private static final ColumnIdentifier<String> PERMISSION = new SimpleColumnIdentifier<>("permission", String.class);
    private static final ColumnIdentifier<Long> USER_ID      = new SimpleColumnIdentifier<>("userId", Long.class);
    private static final ColumnIdentifier<Integer> PERMISSION_ID = new SimpleColumnIdentifier<>("permissionId", Integer.class);

    private Table permissions;
    private Table userPermissions;

    private SQLDriver driver;
    private int nextPermissionId;

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        this.driver = HalpbotUtils.context().get(SQLManager.class)
            .getDriver("halpbotcore", this::populateDatabaseWithDefaultPermissions);
        this.driver.onLoad(this::cacheData);
    }

    private void cacheData(Connection connection) throws SQLException {
        ResultSet permissionsRS = this.driver.executeQuery(connection, "SELECT * FROM permissions");
        this.permissions = SQLUtils.asTable(permissionsRS, PERMISSION_ID, PERMISSION);
        this.nextPermissionId = this.permissions.count() + 1;

        ResultSet userPermissionsRS = this.driver.executeQuery(connection, "SELECT * FROM userPermissions");
        this.userPermissions = SQLUtils.asTable(userPermissionsRS, USER_ID, PERMISSION_ID);
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
                    HalpbotPermissions.OWNER,
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

        this.userPermissions.addRow(userId, permissionId);
        try (Connection connection = this.driver.createConnection()) {
            this.driver.executeUpdate(connection, "INSERT INTO userPermissions (userId, permissionId) VALUES (?, ?)",
                userId, permissionId);
        }
        catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }

    /**
     * Adds the following permissions to the database.
     *
     * @param permissions
     *      The {@link List} of permissions to add to the database
     */
    @Override
    public void createPermissions(List<String> permissions) {
        this.insertPermissionToDatabase(this.driver, permissions);
        for (String permission : permissions) {
            this.permissions.addRow(this.nextPermissionId, permission.toLowerCase(Locale.ROOT));
            this.nextPermissionId++;
        }
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
     * {@link HalpbotPermissions#OWNER} permission, then this will always return true as the owner has all permissions.
     *
     * @param userId
     *      The id of the user to check for the permissions
     * @param permissions
     *      The permissions to check that the user has
     *
     * @return If the user has the specified permissions
     */
    @Override
    public boolean hasPermissions(long userId, String... permissions) {
        List<String> userPermissions = this.getPermissions(userId);
        if (userPermissions.contains(HalpbotPermissions.OWNER))
            return true;
        for (String permission : permissions) {
            if (!userPermissions.contains(permission))
                return false;
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
        try {
            this.userPermissions.joinOn(this.permissions, PERMISSION_ID, false)
                .where(USER_ID, userId)
                .forEach(row -> row.value(PERMISSION)
                    .present(permissions::add));
        }
        catch (IdentifierMismatchException | ValueMismatchException e) {
            ErrorManager.handle(e);
        }
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
