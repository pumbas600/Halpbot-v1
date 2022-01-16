package nz.pumbas.halpbot.utilities;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = false)
@RequiredArgsConstructor
@ComponentBinding(StringTraverser.class)
public class HalpbotStringTraverser implements StringTraverser
{
    private final String content;

    @Setter
    private int currentIndex;

    @Override
    public void incrementIndex() {
        this.currentIndex++;
    }
}
