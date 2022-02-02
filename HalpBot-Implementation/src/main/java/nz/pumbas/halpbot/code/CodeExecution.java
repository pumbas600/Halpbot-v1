package nz.pumbas.halpbot.code;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.mapping.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeExecution
{
    private String language;
    private String version = "*";
    private List<CodeFile> files = new ArrayList<>();

    public CodeExecution(String language, String content) {
        this.language = language;
        this.files.add(new CodeFile(content));
        this.files.add(null);
    }

    public static Exceptional<String> json(ObjectMapper mapper, String language, String content) {
        return mapper.write(new CodeExecution(language, content))
                .map(json -> json.replace(", null", ""));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CodeFile
    {
        private String content;
    }
}
