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

package net.pumbas.halpbot.converters.tokens;

import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.CustomConstructor;
import net.pumbas.halpbot.commands.annotations.CustomParameter;
import net.pumbas.halpbot.converters.ConverterHandler;
import net.pumbas.halpbot.utilities.HalpbotStringTraverser;
import net.pumbas.halpbot.utilities.Reflect;
import net.pumbas.halpbot.utilities.StringTraverser;

import org.dockbox.hartshorn.application.ExceptionHandler;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import lombok.Getter;

@Service
public class HalpbotTokenService implements TokenService {

    private final Map<ExecutableElementContext<?, ?>, List<Token>> cache = new ConcurrentHashMap<>();
    private final Map<TypeContext<?>, String> typeAliases = new ConcurrentHashMap<>();

    @Inject
    @Getter
    private ApplicationContext applicationContext;
    @Inject
    private TokenFactory tokenFactory;
    @Inject
    private ConverterHandler converterHandler;

    @Override
    public String typeAlias(final TypeContext<?> typeContext) {
        if (!this.typeAliases.containsKey(typeContext)) {
            final String alias;
            if (typeContext.annotation(CustomParameter.class).present())
                alias = typeContext.annotation(CustomParameter.class).get().identifier();
            else if (typeContext.isArray())
                alias = this.typeAlias(typeContext.elementType().get()) + "[]";
            else if (typeContext.isPrimitive())
                alias = Reflect.wrapPrimative(typeContext).name();
            else
                alias = typeContext.name();
            this.typeAliases.put(typeContext, alias);
        }

        return this.typeAliases.get(typeContext);
    }

    @Override
    public List<Token> tokens(final ExecutableElementContext<?, ?> executableContext) {
        if (this.cache.containsKey(executableContext))
            return this.cache.get(executableContext);

        final List<Token> tokens = new ArrayList<>();
        final List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        final Result<String> command = this.command(executableContext);
        if (command.present() && !command.get().isBlank()) {
            final StringTraverser stringTraverser = new HalpbotStringTraverser(command.get());

            while (stringTraverser.hasNext()) {
                if (parameterIndex < parameters.size()) {
                    final ParameterContext<?> currentParameter = parameters.get(parameterIndex);
                    final int currentIndex = stringTraverser.currentIndex();

                    if (!this.converterHandler.isCommandParameter(currentParameter) ||
                        stringTraverser.next().equalsIgnoreCase(this.typeAlias(currentParameter.type()))) {
                        tokens.add(this.tokenFactory.createParsing(currentParameter));
                        parameterIndex++;
                        continue;
                    }

                    stringTraverser.currentIndex(currentIndex);
                }
                final PlaceholderToken placeholderToken = stringTraverser.nextSurrounded("[", "]")
                    .map(placeholder -> this.tokenFactory.createPlaceholder(true, placeholder))
                    .orElse(() -> stringTraverser.nextSurrounded("<", ">")
                        .map(placeholder -> this.tokenFactory.createPlaceholder(false, placeholder))
                        .orNull())
                    .orThrowUnchecked(() -> new ApplicationException(
                        "Placeholders must be surrounded by either '[...]' or '<...>' in the command %s"
                            .formatted(executableContext.qualifiedName())));

                tokens.add(placeholderToken);
            }
            if (parameterIndex < parameters.size())
                while (parameterIndex < parameters.size()) {
                    if (this.converterHandler.isCommandParameter(parameters.get(parameterIndex++)))
                        ExceptionHandler.unchecked(new ApplicationException(
                            "All command parameters must be specified in the command"));
                }

        }
        else tokens.addAll(executableContext.parameters()
            .stream()
            .map(parameterContext -> this.tokenFactory.createParsing(parameterContext))
            .collect(Collectors.toList()));

        this.cache.put(executableContext, tokens);
        return tokens;
    }

    private Result<String> command(final ExecutableElementContext<?, ?> executableContext) {
        return executableContext.annotation(Command.class)
            .map(Command::command)
            .orElse(() -> executableContext.annotation(CustomConstructor.class).map(CustomConstructor::command).orNull());

    }
}
