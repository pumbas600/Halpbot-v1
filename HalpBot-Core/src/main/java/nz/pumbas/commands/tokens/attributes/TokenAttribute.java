package nz.pumbas.commands.tokens.attributes;

import java.lang.annotation.Annotation;
import java.util.Optional;

import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.enums.Tristate;

public interface TokenAttribute
{
    /**
     * @return The {@link Annotation} defined by this {@link TokenAttribute}
     */
    Class<? extends Annotation> getAttributeType();

    /**
     * @return If the {@link TokenAttribute#matches(ParsingToken, InvocationContext)} should be called before or
     * after the default token matches functionality.
     */
    boolean checkBeforeTokenMatches();

    /**
     * Returns if the {@link InvocationContext} for the specified {@link ParsingToken} token matches the
     * requirements of this {@link TokenAttribute}.
     * <pre>
     * Note that different return types have different meanings:
     * {@link Tristate#TRUE}    - The invocation token matches and you don't need to check the default matching criteria for this token
     * {@link Tristate#UNKNOWN} - If the invocation token matches should be determined by the default matching critieria
     * {@link Tristate#FALSE}   - The invocation token doesn't match and you don't need to check the default matching criteria.
     * </pre>
     *
     * @param token
     *      The {@link ParsingToken} currently being checked
     * @param invocationToken
     *      The {@link InvocationContext}
     *
     * @return The {@link Tristate} containing is it matches
     */
    Tristate matches(ParsingToken token, InvocationContext invocationToken);

    /**
     * If {@link Optional#empty()} is returned, then the default invocation is used, otherwise the object contained
     * within the {@link Optional} is returned.
     *
     * @param token
     *      The {@link ParsingToken} currently being invoked
     * @param invocationToken
     *      The {@link InvocationContext}
     *
     * @return An {@link Optional} containing the parsed result
     */
    Optional<Object> invoke(ParsingToken token, InvocationContext invocationToken);
}
