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

package net.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.commands.CommandAdapter;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.decorators.DecoratorService;
import net.pumbas.halpbot.permissions.PermissionDecorator;
import net.pumbas.halpbot.utilities.HalpbotUtils;
import net.pumbas.halpbot.utilities.Reflect;
import net.pumbas.halpbot.utilities.Require;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

@Service
public class HalpbotHelpService implements HelpService {

    private final Map<CommandContext, MessageEmbed> commandHelpEmbeds = new ConcurrentHashMap<>();
    @Inject
    private DecoratorService decoratorService;
    @Nullable
    private MessageEmbed allCommandHelpEmbed;

    @Override
    public MessageEmbed build(final CommandAdapter commandAdapter) {
        if (this.allCommandHelpEmbed == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("HALP");

            final Map<Object, List<CommandContext>> commands = commandAdapter.commands()
                .values()
                .stream()
                .collect(Collectors.groupingBy(ActionInvokable::instance));

            final List<Object> sortedKeys = commands.keySet()
                .stream()
                .sorted(Comparator.comparing(key -> -commands.get(key).size()))
                .toList();

            for (final Object instance : sortedKeys) {
                final String title = HalpbotUtils.splitVariableName(
                    TypeContext.unproxy(commandAdapter.applicationContext(), instance).name());
                final StringBuilder stringBuilder = new StringBuilder();
                // distinct() prevents double ups from multiple aliases occuring
                final List<String> commandAliases = commands.get(instance)
                    .stream()
                    .distinct()
                    .map(CommandContext::aliasesString)
                    .sorted()
                    .toList();

                for (final String aliases : commandAliases) {
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
    public MessageEmbed build(final CommandAdapter commandAdapter, final CommandContext commandContext) {
        if (!this.commandHelpEmbeds.containsKey(commandContext)) {
            final EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.cyan)
                .setTitle(commandContext.aliasesString())
                .addField("Usage", commandContext.toString(), false);

            if (!commandContext.description().isBlank())
                embedBuilder.setDescription(commandContext.description());

            final List<PermissionDecorator<?>> permissionDecorators = Reflect.cast(
                this.decoratorService.decorators(commandContext.actionInvokable(), PermissionDecorator.class));

            if (!permissionDecorators.isEmpty()) {
                final String permissions = this.permissions(permissionDecorators);
                embedBuilder.addField("Permissions", permissions, false);
            }
            this.commandHelpEmbeds.put(commandContext, embedBuilder.build());
        }
        return this.commandHelpEmbeds.get(commandContext);
    }

    public String permissions(final List<PermissionDecorator<?>> permissionDecorators) {
        final StringBuilder permissions = new StringBuilder();
        final boolean notAllAndMerger = permissionDecorators.stream()
            .anyMatch((decorator) -> decorator.require() != Require.ALL);

        for (int i = 0; i < permissionDecorators.size(); i++) {
            final PermissionDecorator<?> decorator = permissionDecorators.get(i);
            final String delimiter = " %s ".formatted(decorator.require().name().toLowerCase(Locale.ROOT));

            if (notAllAndMerger)
                permissions.append("(");

            final List<String> decoratorPermissions = decorator.userPermissions()
                .stream()
                .map((permission) -> HalpbotUtils.capitaliseWords(permission.getName().replace('_', ' ')))
                .collect(Collectors.toList());
            decoratorPermissions.addAll(decorator.customPermissions());

            permissions.append(String.join(delimiter, decoratorPermissions));

            if (notAllAndMerger)
                permissions.append(")");
            if (i != permissionDecorators.size() - 1)
                permissions.append(" and ");
        }
        return permissions.toString();
    }
}
