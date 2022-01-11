package nz.pumbas.halpbot.mocks;

import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.utils.config.AuthorizationConfig;
import net.dv8tion.jda.internal.utils.config.MetaConfig;
import net.dv8tion.jda.internal.utils.config.SessionConfig;
import net.dv8tion.jda.internal.utils.config.ThreadingConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockJDA extends JDAImpl
{
    public static final MockJDA INSTANCE = new MockJDA();
    @Nullable private SelfUser selfUser;

    public MockJDA() {
        super(
                new AuthorizationConfig("TEST"),
                SessionConfig.getDefault(),
                ThreadingConfig.getDefault(),
                MetaConfig.getDefault()
        );
    }

    @NotNull
    @Override
    public SelfUser getSelfUser() {
        if (null == this.selfUser)
            this.selfUser = new SelfUserImpl(0, INSTANCE);
        return this.selfUser;
    }
}
