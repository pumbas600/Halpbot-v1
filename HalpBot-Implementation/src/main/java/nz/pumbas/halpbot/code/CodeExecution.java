package nz.pumbas.halpbot.code;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

    @JsonInclude(value = Include.NON_NULL, content = Include.NON_NULL)
    private List<CodeFile> files = new ArrayList<>();

    public CodeExecution(String language, String content) {
        this.language = language;
        this.files.add(new CodeFile(content));
        this.files.add(null);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CodeFile
    {
        private String content;
    }
}
