package nz.pumbas.halpbot.code;

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
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CodeFile
    {
        private String content;
    }
}
