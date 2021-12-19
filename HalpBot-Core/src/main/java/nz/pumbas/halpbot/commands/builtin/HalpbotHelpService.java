package nz.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import org.dockbox.hartshorn.core.annotations.inject.Binds;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.commands.Invokable;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Binds(HelpService.class)
public class HalpbotHelpService implements HelpService
{
    @Override
    public MessageEmbed build(CommandAdapter commandAdapter) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("HALP");

        Map<Object, List<CommandContext>> commands = commandAdapter.registeredCommands()
                .values()
                .stream()
                .collect(Collectors.groupingBy(Invokable::instance));

        for (Entry<Object, List<CommandContext>> entry : commands.entrySet()) {
            String title = HalpbotUtils.splitVariableName(entry.getKey().getClass().getSimpleName());
            StringBuilder stringBuilder = new StringBuilder();
            for (CommandContext commandContext : new HashSet<>(entry.getValue())) {
                stringBuilder.append("- ")
                        .append(String.join(" | ", commandContext.aliases()))
                        .append("\n");
            }

            embedBuilder.addField(title, stringBuilder.toString(), true);
        }

        return embedBuilder.build();
    }

    @Override
    public MessageEmbed build(Guild guild, CommandAdapter commandAdapter, CommandContext commandContext) {
        String prefix = commandAdapter.prefix(guild.getIdLong());

        EmbedBuilder embedBuilder =  new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle("%s %s".formatted(prefix, commandContext.aliasesString()))
                .addField("Usage", "%s %s".formatted(prefix, commandContext.toString()), false);

        if (!commandContext.description().isBlank())
            embedBuilder.setDescription(commandContext.description());
        if (!commandContext.permissions().isEmpty())
            embedBuilder.addField("Permissions", String.join(", ", commandContext.permissions()), false);

        return embedBuilder.build();
    }
}
