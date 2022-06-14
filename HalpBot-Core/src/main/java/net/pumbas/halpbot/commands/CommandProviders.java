package net.pumbas.halpbot.commands;

import net.pumbas.halpbot.commands.actioninvokable.CommandInvokable;
import net.pumbas.halpbot.commands.actioninvokable.HalpbotCommandInvokable;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.commands.actioninvokable.context.HalpbotCommandInvocationContext;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.commands.actioninvokable.context.command.HalpbotCommandContext;
import net.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import net.pumbas.halpbot.commands.actioninvokable.context.constructor.HalpbotCustomConstructorContext;
import net.pumbas.halpbot.commands.annotations.UseCommands;
import net.pumbas.halpbot.commands.builtin.HalpbotHelpService;
import net.pumbas.halpbot.commands.builtin.HelpService;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
@RequiresActivator(UseCommands.class)
public class CommandProviders {

    @Provider
    public Class<? extends CommandAdapter> commandAdapter() {
        return HalpbotCommandAdapter.class;
    }

    @Provider
    public Class<? extends HelpService> helpService() {
        return HalpbotHelpService.class;
    }

    @Provider
    public Class<? extends CommandContext> commandContext() {
        return HalpbotCommandContext.class;
    }

    @Provider
    public Class<? extends CustomConstructorContext> customConstructorContext() {
        return HalpbotCustomConstructorContext.class;
    }

    @Provider
    public Class<? extends CommandInvocationContext> commandInvocationContext() {
        return HalpbotCommandInvocationContext.class;
    }

    @Provider
    public Class<? extends CommandInvokable> commandInvokable() {
        return HalpbotCommandInvokable.class;
    }
}
