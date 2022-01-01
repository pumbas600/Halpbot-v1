package nz.pumbas.halpbot.configurations;

import org.dockbox.hartshorn.config.annotations.Configuration;
import org.dockbox.hartshorn.config.annotations.Value;
import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.data.FileFormats;
import org.dockbox.hartshorn.data.remote.DerbyFileRemote;
import org.dockbox.hartshorn.data.remote.JdbcRemoteConfiguration;
import org.dockbox.hartshorn.data.remote.PersistenceConnection;
import org.dockbox.hartshorn.data.remote.Remote;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.permissions.repositories.PermissionRepository;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

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

    @Value("ownerId")
    private long ownerId = -1;

    @Provider
    @Singleton
    public PermissionRepository permissionRepository(ApplicationContext applicationContext)
            throws ApplicationException
    {
        Path path = new File("Halpbot-Core-DB").toPath();
        // TODO: Create DerbyFileRemote without username and password
        PersistenceConnection connection = DerbyFileRemote.INSTANCE.connection(path, "root", "demo");
        return (PermissionRepository) applicationContext.get(PermissionRepository.class).connection(connection);
    }

}
