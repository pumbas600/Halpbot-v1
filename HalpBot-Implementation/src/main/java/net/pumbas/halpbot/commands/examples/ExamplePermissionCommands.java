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

package net.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.Permission;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.permissions.HalpbotPermissions;
import net.pumbas.halpbot.permissions.Permissions;
import net.pumbas.halpbot.utilities.Require;

import org.dockbox.hartshorn.component.Service;

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

    @Permissions(user = {Permission.MANAGE_PERMISSIONS, Permission.MANAGE_ROLES}, merger = Require.ANY)
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
