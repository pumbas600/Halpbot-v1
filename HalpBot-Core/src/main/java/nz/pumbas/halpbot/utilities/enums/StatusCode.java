package nz.pumbas.halpbot.utilities.enums;

import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.commands.ErrorManager;

public enum StatusCode
{
    UNKNOWN(0, false),
    OK(200),
    BAD_REQUEST(400),
    UNAUTHORISED(401),
    FORBIDDEN(403),
    NOT_FOUND(404);

    private static final Map<Integer, StatusCode> mappings = new HashMap<>();

    static {
        for (StatusCode statusCode : values()) {
            mappings.put(statusCode.getCode(), statusCode);
        }
    }

    public static StatusCode of(String code) {
        if (null != code) {
            try {
                int parsedCode = Integer.parseInt(code);
                return of(parsedCode);
            } catch (NumberFormatException e) {
                ErrorManager.handle(e);
            }
        }
        return StatusCode.UNKNOWN;
    }

    public static StatusCode of(int code) {
        return mappings.getOrDefault(code, StatusCode.UNKNOWN);
    }

    private final int code;
    private final boolean successful;

    StatusCode(int code) {
        this.code = code;
        this.successful = 299 > code;
    }

    StatusCode(int code, boolean successful) {
        this.code = code;
        this.successful = successful;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isSuccessful() {
        return this.successful;
    }
}
