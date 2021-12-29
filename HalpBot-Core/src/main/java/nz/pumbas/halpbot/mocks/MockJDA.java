package nz.pumbas.halpbot.mocks;

import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;

public class MockJDA extends JDAImpl
{
    public static final MockJDA INSTANCE = new MockJDA();

    public MockJDA() {
        super(
                new AuthorizationConfig("TEST"),
                SessionConfig.getDefault(),
                ThreadingConfig.getDefault(),
                MetaConfig.getDefault()
        );
    }
}
