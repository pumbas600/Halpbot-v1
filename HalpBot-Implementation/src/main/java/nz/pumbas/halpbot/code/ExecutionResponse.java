package nz.pumbas.halpbot.code;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionResponse
{
    private String language;
    private String version;
    private Run run;

    @Getter
    @Setter
    public static class Run {
        private String stdout;
        private String stderr;
        private int code;
    }
}
