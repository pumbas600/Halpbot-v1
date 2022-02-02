package nz.pumbas.halpbot.configurations;


import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.data.annotations.Configuration;
import org.dockbox.hartshorn.data.annotations.Value;

import lombok.Getter;

@Getter
@Service
@Configuration(source = "classpath:bot-config.properties")
public class BotConfiguration
{
    @Value("defaultPrefix")
    private String defaultPrefix = "";

    @Value("displayConfiguration")
    private String displayConfiguration = "nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration";

    @Value("usageBuilder")
    private String usageBuilder = "nz.pumbas.halpbot.commands.usage.TypeUsageBuilder";

    @Value("ownerId")
    private long ownerId = -1;

    @Value("useRoleBinding")
    private boolean useRoleBinding;
}
