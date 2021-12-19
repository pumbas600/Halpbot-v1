package nz.pumbas.halpbot.actions.cooldowns;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

public class CooldownTimer
{
    public static final CooldownTimer Empty = new CooldownTimer(0);

    private final long startTimeMs;
    private final long durationMs;

    public CooldownTimer(long durationMs) {
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = durationMs;
    }

    public boolean hasFinished() {
        return System.currentTimeMillis() - this.startTimeMs > this.durationMs;
    }

    public long getRemainingTime() {
        return this.durationMs - (System.currentTimeMillis() - this.startTimeMs);
    }

    public MessageEmbed getRemainingTimeEmbed() {
        double remainingTimeSeconds = this.getRemainingTime() / 1000D;

        return new EmbedBuilder()
            .setTitle("Please wait, you're on cooldown")
            .setDescription(String.format("%.2fs Remaining", remainingTimeSeconds))
            .setColor(Color.BLUE)
            .build();
    }
}
