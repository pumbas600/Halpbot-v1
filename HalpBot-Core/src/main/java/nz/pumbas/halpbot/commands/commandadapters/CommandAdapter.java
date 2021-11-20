package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;

@Service
public interface CommandAdapter extends HalpbotAdapter
{
    void onMessageReceived(@NotNull MessageReceivedEvent event);

    @NotNull
    String prefix();

    <T> void registerCommands(@NotNull TypeContext<T> typeContext);

    @NotNull
    default Exceptional<CommandContext> commandContextSafely(@NotNull String alias) {
        return Exceptional.of(this.commandContext(alias));
    }

    @Nullable
    CommandContext commandContext(@NotNull String alias);

    @NotNull
    Map<String, CommandContext> registeredCommands();
}
