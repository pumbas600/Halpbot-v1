package nz.pumbas.commands.tokens.attributes;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.tokens.tokentypes.ArrayToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Tristate;

public class ImplicitTokenAttribute implements TokenAttribute
{
    /**
     * @return The {@link Annotation} defined by this {@link TokenAttribute}
     */
    @Override
    public Class<? extends Annotation> getAttributeType()
    {
        return Implicit.class;
    }

    /**
     * @return If the {@link TokenAttribute#matches(ParsingToken, InvocationTokenInfo)} should be called before or
     *     after the default token matches functionality.
     */
    @Override
    public boolean checkBeforeTokenMatches()
    {
        return true;
    }

    /**
     * Returns if the {@link InvocationTokenInfo} for the specified {@link ParsingToken} token matches the
     * requirements of this {@link TokenAttribute}.
     * <pre>
     * Note that different return types have different meanings:
     * {@link Tristate#TRUE}    - The invocation token matches and you don't need to check the default matching criteria for this token
     * {@link Tristate#UNKNOWN} - If the invocation token matches should be determined by the default matching critieria
     * {@link Tristate#FALSE}   - The invocation token doesn't match and you don't need to check the default matching criteria.
     * </pre>
     *
     * @param token
     *     The {@link ParsingToken} currently being checked
     * @param invocationToken
     *     The {@link InvocationTokenInfo}
     *
     * @return The {@link Tristate} containing is it matches
     */
    @Override
    public Tristate matches(ParsingToken token, InvocationTokenInfo invocationToken)
    {
        if (token instanceof ArrayToken && Reflect.hasAnnotation(token.getAnnotations(), Implicit.class))
        {
            ArrayToken arrayToken = (ArrayToken) token;
            invocationToken.saveState(this);
            if (arrayToken.getCommandToken().matches(invocationToken)) {
                invocationToken.saveState(this);
                //Loop through as many possible tokens that match.
                while (invocationToken.hasNext()) {
                    if (arrayToken.getCommandToken().matches(invocationToken))
                        invocationToken.saveState(this);
                    else {
                        invocationToken.restoreState(this);
                        break;
                    }
                }
                return Tristate.TRUE;
            }
        }
        return Tristate.UNKNOWN;
    }

    /**
     * If {@link Optional#empty()} is returned, then the default invocation is used, otherwise the object contained
     * within the {@link Optional} is returned.
     *
     * @param token
     *     The {@link ParsingToken} currently being invoked
     * @param invocationToken
     *     The {@link InvocationTokenInfo}
     *
     * @return An {@link Optional} containing the parsed result
     */
    @Override
    public Optional<Object> invoke(ParsingToken token, InvocationTokenInfo invocationToken)
    {
        if (token instanceof ArrayToken && Reflect.hasAnnotation(token.getAnnotations(), Implicit.class))
        {
            List<Object> parsedArray = new ArrayList<>();
            ArrayToken arrayToken = (ArrayToken) token;
            invocationToken.saveState(this);
            if (arrayToken.getCommandToken().matches(invocationToken)) {
                invocationToken.restoreState(this);
                do {
                    parsedArray.add(arrayToken.getCommandToken().parse(invocationToken));
                    invocationToken.saveState(this);

                    if (!arrayToken.getCommandToken().matches(invocationToken)) {
                        invocationToken.restoreState(this);
                        break;
                    }
                    invocationToken.restoreState(this);
                }
                while (invocationToken.hasNext());
            }

            return Optional.of(parsedArray);
        }
        return Optional.empty();
    }
}
