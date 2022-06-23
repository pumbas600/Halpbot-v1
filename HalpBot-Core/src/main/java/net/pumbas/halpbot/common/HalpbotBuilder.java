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

package net.pumbas.halpbot.common;

import net.dv8tion.jda.api.JDABuilder;
import net.pumbas.halpbot.utilities.ErrorManager;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HalpbotBuilder {

    @Nullable
    private static HalpbotBuilder instance;

    private final Class<?> main;
    private final String[] args;

    @Getter
    @Nullable
    private Function<String, JDABuilder> jdaBuilder;

    public static HalpbotBuilder create(final Class<?> main, final String[] args) {
        if (instance == null) {
            instance = new HalpbotBuilder(main, args);
        }
        else throw new IllegalStateException("HalpbotBuilder already created");
        return instance;
    }

    public static HalpbotBuilder instance() {
        if (instance == null) {
            throw new IllegalStateException("HalpbotBuilder not created");
        }
        return instance;
    }

    public ApplicationContext build(final Function<String, JDABuilder> jdaBuilder) {
        this.jdaBuilder = jdaBuilder;

        final ApplicationContext applicationContext = new HalpbotApplicationFactory()
            .loadDefaults()
            .activator(TypeContext.of(this.main))
            .arguments(this.args)
            .create();

        applicationContext.get(ErrorManager.class); // Create an instance of the ErrorManager
        return applicationContext;
    }
}
