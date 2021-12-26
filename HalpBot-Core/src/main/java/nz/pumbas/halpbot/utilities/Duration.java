package nz.pumbas.halpbot.utilities;

import java.time.temporal.ChronoUnit;

public @interface Duration
{
    long value();

    ChronoUnit unit() default ChronoUnit.SECONDS;
}
