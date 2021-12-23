/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.utilities.enums;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.utilities.ErrorManager;

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

    public static StatusCode of(@Nullable String code) {
        if (code != null) {
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
