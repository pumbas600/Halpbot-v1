package nz.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.permissions.PermissionDecorator;
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
                .collect(Collectors.groupingBy(ActionInvokable::instance));

        List<Object> sortedKeys = commands.keySet()
                .stream()
                .sorted(Comparator.comparing(key -> -commands.get(key).size()))
                .collect(Collectors.toList());

        for (Object instance : sortedKeys) {
            String title = HalpbotUtils.splitVariableName(
                    TypeContext.unproxy(commandAdapter.applicationContext(), instance).name());
            StringBuilder stringBuilder = new StringBuilder();
            for (CommandContext commandContext : new HashSet<>(commands.get(instance))) {
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
        if (commandContext.actionInvokable() instanceof PermissionDecorator<?> permissionDecorator && !permissionDecorator.permissions().isEmpty())
            embedBuilder.addField("Permissions", String.join(", ", permissionDecorator.permissions()), false);

        return embedBuilder.build();
    }
}
