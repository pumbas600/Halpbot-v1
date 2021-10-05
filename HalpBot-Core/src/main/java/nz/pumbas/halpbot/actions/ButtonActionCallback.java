package nz.pumbas.halpbot.actions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.objects.Exceptional;

public class ButtonActionCallback extends AbstractActionCallback implements MethodCallback
{
    private final Method callback;
    private final Object instance;
    private final boolean isEphemeral;

    protected ButtonActionCallback(
        Method callback, Object instance,
        boolean isEphemeral,
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        List<String> permissions, boolean singleUse,
        long displayDuration)
    {
        super(deleteAfterDuration,
            deleteAfterTimeUnit,
            cooldownDuration,
            cooldownTimeUnit,
            permissions,
            singleUse,
            displayDuration);

        this.callback = callback;
        this.instance = instance;
        this.isEphemeral = isEphemeral;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        return this.invoke(event.getEvent(ButtonClickEvent.class));
    }

    @Override
    public Method getMethod() {
        return this.callback;
    }

    @Override
    public @Nullable Object getInstance() {
        return this.instance;
    }

    public boolean isEphemeral() {
        return this.isEphemeral;
    }
}
