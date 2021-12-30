package nz.pumbas.halpbot.actions.cooldowns;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class CooldownTimer
{
    public static final CooldownTimer Empty = new CooldownTimer(Duration.ZERO);

    private final OffsetDateTime end;
    private OffsetDateTime previousRemainingTime = OffsetDateTime.MIN;

    public CooldownTimer(Duration duration) {
        this.end = OffsetDateTime.now().plus(duration);
    }

    public boolean hasFinished() {
        return this.remainingTime() <= 0;
    }

    public long remainingTime() {
        return OffsetDateTime.now().until(this.end, ChronoUnit.MILLIS);
    }

    public boolean canSendEmbed(long secondsPassed) {
        return this.previousRemainingTime.until(OffsetDateTime.now(), ChronoUnit.SECONDS) >= secondsPassed;
    }

    public MessageEmbed remainingTimeEmbed() {
        double remainingTimeSeconds = this.remainingTime() / 1000D;
        this.previousRemainingTime = OffsetDateTime.now();

        return new EmbedBuilder()
            .setTitle("Please wait, you're on cooldown")
            .setDescription(String.format("%.2fs Remaining", remainingTimeSeconds))
            .setColor(Color.BLUE)
            .build();
    }
}
