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

package net.pumbas.halpbot.commands;

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import net.pumbas.halpbot.commands.actioninvokable.CommandInvokable;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.UseCommands;
import net.pumbas.halpbot.decorators.DecoratorService;
import net.pumbas.halpbot.decorators.cooldowns.Cooldown;
import net.pumbas.halpbot.decorators.cooldowns.CooldownDecorator;
import net.pumbas.halpbot.decorators.log.Log;
import net.pumbas.halpbot.decorators.log.LogDecorator;
import net.pumbas.halpbot.permissions.PermissionDecorator;
import net.pumbas.halpbot.permissions.Permissions;
import net.pumbas.halpbot.utilities.Duration;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import jakarta.inject.Inject;


@Cooldown(duration = @Duration(10))
@Permissions(permissions = "halpbot.example.class")
@UseCommands
@Service
@HartshornTest
public class DecoratorServiceTests {

    @Inject
    private CommandAdapter commandAdapter;
    @Inject
    private DecoratorService decoratorService;

    @Log
    @Cooldown
    @Permissions(permissions = "halpbot.example.action")
    @Command(alias = "decoratedCommandTest")
    public void decoratedCommandTestMethod() {
    }

    @Command(alias = "undecoratedCommandTest")
    public void undecoratedCommandTestMethod() {
    }

    @Test
    public void usesActionDecoratorsUnlessMergeSpecifiesOtherwise() {
        final CommandContext commandContext = this.commandAdapter.commandContext("decoratedCommandTest");
        Assertions.assertNotNull(commandContext);

        ActionInvokable<?> actionInvokable = commandContext.actionInvokable();
        Assertions.assertEquals(4, this.decoratorService.depth(actionInvokable));
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CooldownDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(LogDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CommandInvokable.class, actionInvokable);

        Assertions.assertEquals(actionInvokable, this.decoratorService.root(commandContext.actionInvokable()));
    }

    @Test
    public void usesClassDecorators() {
        final CommandContext commandContext = this.commandAdapter.commandContext("undecoratedCommandTest");
        Assertions.assertNotNull(commandContext);

        ActionInvokable<?> actionInvokable = commandContext.actionInvokable();
        Assertions.assertEquals(2, this.decoratorService.depth(actionInvokable));
        Assertions.assertInstanceOf(PermissionDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CooldownDecorator.class, actionInvokable);

        actionInvokable = ((ActionInvokableDecorator<?>) actionInvokable).actionInvokable();
        Assertions.assertInstanceOf(CommandInvokable.class, actionInvokable);

        Assertions.assertEquals(actionInvokable, this.decoratorService.root(commandContext.actionInvokable()));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void findingDecoratorsOfTypeTest() {
        final CommandContext commandContext = this.commandAdapter.commandContext("decoratedCommandTest");
        Assertions.assertNotNull(commandContext);

        final ActionInvokable<?> actionInvokable = commandContext.actionInvokable();
        final List<PermissionDecorator> permissionDecorators =
            this.decoratorService.decorators(actionInvokable, PermissionDecorator.class);
        final List<CooldownDecorator> cooldownDecorators =
            this.decoratorService.decorators(actionInvokable, CooldownDecorator.class);
        final List<LogDecorator> logDecorators =
            this.decoratorService.decorators(actionInvokable, LogDecorator.class);

        Assertions.assertEquals(2, permissionDecorators.size());
        Assertions.assertEquals(1, cooldownDecorators.size());
        Assertions.assertEquals(1, logDecorators.size());
    }
}
