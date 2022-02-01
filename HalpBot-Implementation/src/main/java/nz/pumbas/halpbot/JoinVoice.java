package nz.pumbas.halpbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.permissions.Permissions;
import nz.pumbas.halpbot.triggers.Trigger;
import nz.pumbas.halpbot.utilities.Require;

@Service
public class JoinVoice extends ListenerAdapter
{
    private final long targetId = -1;
    private long currentVoiceChannelId = -1;

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        this.checkIfVoiceChannelIsEmpty(event.getGuild(), event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        this.checkIfVoiceChannelIsEmpty(event.getGuild(), event.getChannelLeft());
    }

    private void checkIfVoiceChannelIsEmpty(Guild guild, VoiceChannel channel) {
        if (channel.getIdLong() == this.currentVoiceChannelId)
            if (channel.getMembers().size() == 1) // Halpbot is the only one still in the voice channel
                guild.getAudioManager().closeAudioConnection();
    }

    @Nullable
    @Permissions(self = { Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS })
    @Trigger(value = { "join", "call", "halpbot" }, require = Require.ALL)
    public String joinCall(@Source Guild guild, @Source Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null)
            return "Voice state information has not be enabled. Make sure to enable the GUILD_VOICE_STATES intent";

        VoiceChannel channel = voiceState.getChannel();
        if (channel == null)
            return "You're not in a voice channel though :sob:";

        int userLimit = channel.getUserLimit();
        List<Member> members = channel.getMembers();
        if (userLimit != 0 && userLimit == members.size())
            return "Sorry, the voice channel is full...";

        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(channel);
        this.currentVoiceChannelId = channel.getIdLong();

        this.findTarget(members)
                .present(target -> guild.kickVoiceMember(target).queue()); // Kick the target from the voice channel

        return null; // Don't respond
    }

    private Exceptional<Member> findTarget(List<Member> members) {
        return Exceptional.of(members.stream()
                .filter(member -> member.getIdLong() == this.targetId)
                .findFirst());
    }
}
