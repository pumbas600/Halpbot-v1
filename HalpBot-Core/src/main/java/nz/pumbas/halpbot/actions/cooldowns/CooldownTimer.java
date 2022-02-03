/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    public MessageEmbed remainingTimeEmbed(String title) {
        double remainingTimeSeconds = this.remainingTime() / 1000D;
        this.previousRemainingTime = OffsetDateTime.now();

        return new EmbedBuilder()
            .setTitle(title)
            .setDescription(String.format("%.2fs Remaining", remainingTimeSeconds))
            .setColor(Color.BLUE)
            .build();
    }
}
