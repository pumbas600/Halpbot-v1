package nz.pumbas.halpbot.code;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Language
{
    private String language;
    private String version;
    private List<String> aliases;

    public List<String> allAliases() {
        List<String> copy = new ArrayList<>(this.aliases);
        copy.add(this.language);
        return copy;
    }
}
