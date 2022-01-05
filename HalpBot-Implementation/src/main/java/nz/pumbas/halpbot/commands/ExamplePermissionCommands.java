package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.Permission;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.Merger;
import nz.pumbas.halpbot.permissions.Permissions;

@Service
@Permissions(user = Permission.MANAGE_PERMISSIONS)
public class ExamplePermissionCommands
{
    @Command(description = "Testing class level permissions")
    public String classPermission() {
        return "You need the *MANAGE_PERMISSIONS* permission to use this command";
    }

    @Permissions(user = Permission.BAN_MEMBERS)
    @Command(description = "Testing class level permissions")
    public String actionPermission() {
        return "You need the *MANAGE_PERMISSIONS* and *BAN_MEMBERS* permissions to use this command";
    }

    @Permissions(permissions = HalpbotPermissions.GUILD_OWNER)
    @Command(description = "Tests the @Permissions decorator")
    public String permission() {
        return "This command is restricted to people who own the guild this command was invoked in";
    }

    @Permissions(user = {Permission.MANAGE_PERMISSIONS, Permission.MANAGE_ROLES}, merger = Merger.OR)
    @Command(description = "Tests the OR merger in the @Permission decorator")
    public String orPermissions() {
        return "This command is restricted to people with the *MANAGE_PERMISSIONS* or *MANAGE_ROLES* permissions";
    }

    @Permissions(permissions = "halpbot.example.testC")
    @Command(description = "Tests custom permissions")
    public String customPermissions() {
        return "This command is restricted to people with the *halpbot.example.testA* permission";
    }

    // Note: This combination of permissions doesn't really make sense and is only for demonstration purposes.
    @Permissions(permissions = {"halpbot.example.testB", HalpbotPermissions.GUILD_OWNER})
    @Command(description = "Tests multiple custom permissions")
    public String multipleCustomPermissions() {
        return "This command is restricted to people with the *halpbot.example.testB* permission and are the guild owner";
    }

//    @Permissions({Permission.ADMINISTRATOR, Permission.MANAGE_SERVER})
//    @Command(description = "Tests the @Permission decorator")
//    public String permission() {
//        return "This command is restricted to people with the *ADMINISTRATOR* and *MANAGE_SERVER* permissions";
//    }
}
