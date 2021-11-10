package nz.pumbas.halpbot.commands.servicescanners;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServiceOrder;
import org.dockbox.hartshorn.core.services.ServiceProcessor;
import org.jetbrains.annotations.NotNull;

import lombok.SneakyThrows;
import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.adapters.exceptions.IllegalAdapterException;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;

public class CommandServiceScanner implements ServiceProcessor<UseCommands>
{
    @Override
    public ServiceOrder order() {
        return ServiceOrder.LATE;
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return !type.methods(Command.class).isEmpty();
    }

    @SneakyThrows
    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        final HalpbotCore halpbotCore = context.get(HalpbotCore.class);
        final UseCommands activator = context.activator(UseCommands.class);

        if (!HalpbotAdapter.class.isAssignableFrom(activator.adapter()))
            throw new IllegalAdapterException("The command adapter %s should extend HalpbotAdapter"
                .formatted(activator.adapter().getCanonicalName()));

        this.registerCommands(halpbotCore, type, activator.adapter());
    }

    @SuppressWarnings("unchecked")
    private <T extends HalpbotAdapter & CommandAdapter> void registerCommands(@NotNull HalpbotCore halpbotCore,
                                                                              @NotNull TypeContext<?> type,
                                                                              @NotNull Class<? extends CommandAdapter> adapter)
    {
        halpbotCore.getAndRegister((TypeContext<T>) TypeContext.of(adapter))
            .registerCommands(type);
    }
}
