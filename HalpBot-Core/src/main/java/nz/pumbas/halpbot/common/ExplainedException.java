package nz.pumbas.halpbot.common;

import lombok.Getter;

public class ExplainedException extends RuntimeException
{
    @Getter private final Object explanation;

    public ExplainedException(Object explanation) {
        this.explanation = explanation;
    }

    public ExplainedException(String message) {
        super(message);
        this.explanation = message;
    }

    public ExplainedException(String message, Throwable cause) {
        super(message, cause);
        this.explanation = message;
    }

    public ExplainedException(Object explanation, Throwable cause) {
        super(cause);
        this.explanation = explanation;
    }
}
