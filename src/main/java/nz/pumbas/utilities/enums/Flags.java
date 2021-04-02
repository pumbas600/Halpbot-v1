package nz.pumbas.utilities.enums;

import java.util.ArrayList;
import java.util.List;

public final class Flags
{
    private Flags() {}

    /**
     * Converts an {@link Enum flag} into its corresponding bit-id, which is just 2 to the power of the flags
     * ordinal - 1 (The equivalent of bit-shifting 1), or 0 if the ordinal is 0. This gives the following ordinal to
     * bit-id conversions:
     * 0 : 0000
     * 1 : 0001
     * 2 : 0010
     * 3 : 0100
     * ...
     *
     * @param flag
     *      The {@link Enum flag} to retrieve its id for
     *
     * @return The bit-id of the {@link Enum flag}
     */
    public static int getBitId(Enum<?> flag)
    {
        return 0 == flag.ordinal() ? 0 : 1 << flag.ordinal() - 1;
    }

    /**
     * Determines if the flag mask contains the specified {@link Enum flag}. This can be determined by using the bit-wise and
     * operator as shown in the following example:
     * flag-a : 0001
     * flag-b : 0010
     *
     * flag-a | flag-b : 0011 (flag mask)
     * flag mask : 0011
     * &  flag-b : 0010
     * --------- : 0010 -> flag-b is in the flag mask.
     *
     * @param flagMask
     *      The combined flags to check if it contains the specified {@link Enum flag}
     * @param flag
     *      The {@link Enum flag} to check the flag mask for
     * @param <T>
     *      The type of the {@link Enum flag}
     *
     * @return Whether the flag mask contains the specified {@link Enum flag}
     */
    public static <T extends Enum<?>> boolean hasFlag(int flagMask, T flag)
    {
        int bitId = getBitId(flag);
        return (flagMask & bitId) == bitId;
    }

    /**
     * Gets a {@link List} of the flags within the flag mask.
     *
     * @param flagMask
     *      The flag mask to retrieve the flags from
     * @param flagType
     *      The {@link Class} of the flags being retrieved
     * @param <T>
     *      The type of the flags
     *
     * @return The {@link List} of flags within the flag mask
     */
    public static <T extends Enum<?>> List<T> getFlags(int flagMask, Class<T> flagType)
    {
        List<T> flags = new ArrayList<>();
        for (T flag : flagType.getEnumConstants()) {
            if (hasFlag(flagMask, flag))
                flags.add(flag);
        }
        return flags;
    }
}
