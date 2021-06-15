package nz.pumbas.commands.tokens.tokensyntax;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.utilities.Reflect;

public class CommandTokenInfo
{
    private final List<String> tokens;
    private int currentTokenIndex;

    private final Class<?>[] parameterTypes;
    private final Annotation[][] parameterAnnotations;
    private int currentParameterTypeIndex;

    private final Map<TokenBindingIdentifier, Object> currentTokenBindings = new HashMap<>();
    private String currentToken;

    public CommandTokenInfo(List<String> tokens, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations,
                            int currentParameterTypeIndex)
    {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.parameterTypes = parameterTypes;
        this.parameterAnnotations = parameterAnnotations;
        this.currentParameterTypeIndex = currentParameterTypeIndex;
    }

    /**
     * Retrieves the specified {@link Annotation} from the current parameter if present.
     *
     * @param annotationType
     *      The {@link Class type} of the {@link Annotation}
     * @param <T>
     *      The type of the {@link Annotation}
     *
     * @return An {@link Optional} containing the {@link Annotation} if present
     */
    public <T extends Annotation> Optional<T> getAttribute(Class<T> annotationType)
    {
        return Reflect.retrieveAnnotation(this.getAnnotations(), annotationType);
    }

    /**
     * @return The {@link Annotation annotations} for the curent parameter
     */
    public Annotation[] getAnnotations()
    {
        this.checkForCustomParameterTypes();
        return this.parameterAnnotations[this.currentParameterTypeIndex];
    }

    /**
     * Retrieves the specified {@link TokenBindingIdentifier} from the current token.
     *
     * @param identifier
     *      The {@link TokenBindingIdentifier identifier} to retrieve the {@link Object binding} for
     * @param <T>
     *      The type of the {@link Object binding}
     *
     * @return The casted {@link Object} binding
     */
    @SuppressWarnings("unchecked")
    public <T> T getCurrentTokenBinding(TokenBindingIdentifier identifier)
    {
        return (T) this.currentTokenBindings.getOrDefault(identifier, null);
    }

    /**
     * Sets a {@link Object binding} for the current token.
     *
     * @param identifier
     *      The {@link TokenBindingIdentifier} for this {@link Object binding}
     * @param binding
     *      The {@link Object binding} for this {@link TokenBindingIdentifier}
     */
    public void setCurrentTokenBinding(TokenBindingIdentifier identifier, Object binding)
    {
        this.currentTokenBindings.put(identifier, binding);
    }

    /**
     * @return The current {@link String token}
     */
    public String getCurrentToken()
    {
        if (null == this.currentToken)
            this.currentToken = this.tokens.get(this.currentTokenIndex);
        return this.currentToken;
    }

    /**
     * Sets the current token to the new one specified.
     *
     * @param newToken
     *      The token to set the current token to.
     */
    public void setCurrentToken(String newToken)
    {
        this.currentToken = newToken;
    }

    /**
     * @return The current {@link Class parameter type}. If the current {@link Class parameter type} is a wrapper, it
     * will return the primitive equivalent.
     */
    public Class<?> getCurrentParameterType()
    {
        this.checkForCustomParameterTypes();
        return this.parameterTypes[this.currentParameterTypeIndex];
    }

    /**
     * Skips past custom parameter types.
     */
    private void checkForCustomParameterTypes()
    {
        while (this.currentParameterTypeIndex < this.parameterTypes.length && Reflect.isAssignableTo(
            this.parameterTypes[this.currentParameterTypeIndex],TokenManager.getCustomParameterTypes())) {
            this.currentParameterTypeIndex++;
        }
    }

    /**
     * Increments the current parameter index by 1
     */
    public void incrementParameterIndex()
    {
        this.currentParameterTypeIndex++;
    }

    /**
     * Moves to the next token in the {@link List} and resets current token specific fields to prevent accidental carry
     * over.
     */
    public void nextToken()
    {
        this.currentTokenIndex++;
        this.currentToken = null;
        this.currentTokenBindings.clear();
    }

    /**
     * @return If there are still tokens to be checked
     */
    public boolean hasToken()
    {
        this.checkForCustomParameterTypes();
        return this.currentTokenIndex < this.tokens.size();
    }
}
