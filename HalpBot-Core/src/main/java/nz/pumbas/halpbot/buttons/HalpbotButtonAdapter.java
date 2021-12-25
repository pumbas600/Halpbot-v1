package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.ButtonActionCallback;
import nz.pumbas.halpbot.adapters.HalpbotCore;

@Service
@Binds(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    private final Map<String, ButtonActionCallback> registeredButtons = HartshornUtils.emptyMap();

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private HalpbotCore halpbotCore;

    @Override
    public <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext) {

    }

    @Override
    @SubscribeEvent
    public void onButtonClick(ButtonClickEvent event) {

    }
}
