package nz.pumbas.halpbot.actions;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.objects.Exceptional;

public class ButtonActionCallback extends AbstractActionCallback implements MethodCallback
{
    private final ResponseType responseType;
    private final boolean isEphemeral;
    private final Method callback;
    private final Object instance;

    protected ButtonActionCallback(
        ResponseType responseType,
        boolean isEphemeral,
        Method callback,
        Object instance,
        long deleteAfterDuration,
        TimeUnit deleteAfterTimeUnit,
        long cooldownDuration,
        TimeUnit cooldownTimeUnit, List<String> permissions, boolean singleUse)
    {
        super(deleteAfterDuration,
            deleteAfterTimeUnit,
            cooldownDuration,
            cooldownTimeUnit,
            permissions,
            singleUse);
        this.responseType = responseType;
        this.isEphemeral = isEphemeral;
        this.callback = callback;
        this.instance = instance;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        return null;
    }

    @Override
    public Method getMethod() {
        return this.callback;
    }

    @Override
    public @Nullable Object getInstance() {
        return this.instance;
    }

    public ResponseType getResponseType() {
        return this.responseType;
    }

    public boolean isEphemeral() {
        return this.isEphemeral;
    }
}
