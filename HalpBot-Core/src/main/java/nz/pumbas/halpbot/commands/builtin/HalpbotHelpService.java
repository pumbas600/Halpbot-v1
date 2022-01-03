package nz.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.permissions.PermissionDecorator;
import nz.pumbas.halpbot.permissions.Merger;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;

@Binds(HelpService.class)
public class HalpbotHelpService implements HelpService
{
    @Inject private DecoratorService decoratorService;
    @Nullable private MessageEmbed allCommandHelpEmbed;
    private Map<CommandContext, MessageEmbed> commandHelpEmbeds = HartshornUtils.emptyMap();

    @Override
    public MessageEmbed build(CommandAdapter commandAdapter) {
        if (this.allCommandHelpEmbed == null) {
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
                // distinct() prevents double ups from multiple aliases occuring
                List<String> commandAliases = commands.get(instance)
                        .stream()
                        .distinct()
                        .map(CommandContext::aliasesString)
                        .sorted()
                        .collect(Collectors.toList());

                for (String aliases : commandAliases) {
                    stringBuilder.append("- ")
                            .append(aliases)
                            .append("\n");
                }

                embedBuilder.addField(title, stringBuilder.toString(), true);
            }

            this.allCommandHelpEmbed = embedBuilder.build();
        }
        return this.allCommandHelpEmbed;
    }

    @Override
    public MessageEmbed build(CommandAdapter commandAdapter, CommandContext commandContext) {
        if (!this.commandHelpEmbeds.containsKey(commandContext)) {
            final EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(Color.cyan)
                    .setTitle(commandContext.aliasesString())
                    .addField("Usage", commandContext.toString(), false);

            if (!commandContext.description().isBlank())
                embedBuilder.setDescription(commandContext.description());

            List<PermissionDecorator<?>> permissionDecorators = Reflect.cast(
                    this.decoratorService.decorators(commandContext.actionInvokable(), PermissionDecorator.class));

            if (!permissionDecorators.isEmpty()) {
                String permissions = this.permissions(permissionDecorators);
                embedBuilder.addField("Permissions", permissions, false);
            }
            this.commandHelpEmbeds.put(commandContext, embedBuilder.build());
        }
        return this.commandHelpEmbeds.get(commandContext);
    }

    public String permissions(List<PermissionDecorator<?>> permissionDecorators) {
        StringBuilder permissions = new StringBuilder();
        boolean notAllAndMerger = permissionDecorators.stream()
                .anyMatch((decorator) -> decorator.merger() != Merger.AND);

        for (int i = 0; i < permissionDecorators.size(); i++) {
            PermissionDecorator<?> decorator = permissionDecorators.get(i);
            String delimeter = " %s ".formatted(decorator.merger().name().toLowerCase(Locale.ROOT));

            if (notAllAndMerger)
                permissions.append("(");

            List<String> decoratorPermissions = decorator.jdaPermissions()
                    .stream()
                    .map((permission) -> HalpbotUtils.capitaliseWords(permission.getName().replace('_', ' ')))
                    .collect(Collectors.toList());
            decoratorPermissions.addAll(decorator.customPermissions());

            permissions.append(String.join(delimeter, decoratorPermissions));

            if (notAllAndMerger)
                permissions.append(")");
            if (i != permissionDecorators.size() - 1)
                permissions.append(" and ");
        }
        return permissions.toString();
    }
}
