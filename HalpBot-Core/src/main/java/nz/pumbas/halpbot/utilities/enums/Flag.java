package nz.pumbas.halpbot.utilities.enums;

public interface Flag<T extends Enum<T> & Flag<T>>
{
    /**
     * Calls {@link Flags#getBitId(Enum)} to determine this flags bit-id.
     *
     * @return The flags bit-id
     */
    default int getFlag() {
        return Flags.getBitId((Enum<?>) this);
    }
}
