package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.permissions.Permissions;

@Service
@Permissions(permissions = "halpbot.example.class")
public class ExamplePermissionCommands
{
    @Command(description = "Testing class level permissions")
    public String classPermission() {
        return "You need the *halpbot.example.class* permission to use this command";
    }

    @Permissions(permissions = "halpbot.example.action")
    @Command(description = "Testing class level permissions")
    public String actionPermission() {
        return "You need the *halpbot.example.user* AND *halpbot.example.action* permissions to use this command";
    }
}
