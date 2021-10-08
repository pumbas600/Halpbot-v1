package nz.pumbas.halpbot.objects.expiring;

import java.util.Collection;

public interface ExpiringCollection<T> extends Collection<T>
{
    void renew(T value);

    long getExpirationDuration();
}
