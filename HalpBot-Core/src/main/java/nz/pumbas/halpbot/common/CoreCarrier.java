package nz.pumbas.halpbot.common;

import nz.pumbas.halpbot.HalpbotCore;

public interface CoreCarrier
{
    /**
     * @return The halpbot core
     * @implNote This dependency can be injected where necessary
     */
    HalpbotCore halpbotCore();
}
