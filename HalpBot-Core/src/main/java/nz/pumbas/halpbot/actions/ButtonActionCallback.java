package nz.pumbas.halpbot.actions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.actions.methods.MethodCallback;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ButtonActionCallback extends AbstractActionCallback implements MethodCallback, Copyable
{
    private final Method callback;
    private final Object instance;
    private final boolean isEphemeral;
    private final Object[] parameters;

    protected ButtonActionCallback(
        Method callback, Object instance,
        boolean isEphemeral, Object[] parameters,
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
        this.parameters = parameters;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        return this.invoke(
            HalpbotUtils.combine(
                new Object[]{ event.getEvent(ButtonClickEvent.class) },
                this.parameters));
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

    @Override
    public ActionCallbackBuilder copy() {
        return new ActionCallbackBuilder()
            .setButtonAction(this.instance, this.callback)
            .setParameters(this.parameters);
    }
}
