package nz.pumbas.halpbot.configurations;

import org.dockbox.hartshorn.config.annotations.Configuration;
import org.dockbox.hartshorn.config.annotations.Value;
import org.dockbox.hartshorn.data.FileFormats;

import lombok.Getter;

@Getter
@Configuration(source = "classpath:bot-config", filetype = FileFormats.PROPERTIES)
public class BotConfiguration
{
    @Value("defaultPrefix")
    private String defaultPrefix = "";

    @Value("displayConfiguration")
    private String displayConfiguration = "nz.pumbas.halpbot.configurations.SimpleDisplayConfiguration";

    @Value("usageBuilder")
    private String usageBuilder = "nz.pumbas.halpbot.commands.usage.TypeUsageBuilder";

}
