package nz.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;

public interface HelpService
{
    MessageEmbed build(CommandAdapter commandAdapter);

    MessageEmbed build(Guild guild, CommandAdapter commandAdapter, CommandContext commandContext);
}
