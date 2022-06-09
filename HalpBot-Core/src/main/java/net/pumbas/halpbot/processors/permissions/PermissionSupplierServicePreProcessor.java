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

package net.pumbas.halpbot.processors.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.pumbas.halpbot.permissions.PermissionSupplier;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.processing.ServicePreProcessor;
import org.dockbox.hartshorn.inject.Key;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;

public class PermissionSupplierServicePreProcessor implements ServicePreProcessor {

    @Override
    public boolean preconditions(final ApplicationContext context, final Key<?> key) {
        return !key.type().methods(PermissionSupplier.class).isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void process(final ApplicationContext context, final Key<T> key) {
        final TypeContext<T> type = key.type();

        final PermissionSupplierContext permissionSupplierContext = context.first(PermissionSupplierContext.class).get();
        final List<? extends MethodContext<?, T>> permissionSuppliers = type.methods(PermissionSupplier.class);

        for (final MethodContext<?, T> permissionSupplier : permissionSuppliers) {
            if (!this.isValidPermissionSupplier(context, permissionSupplier))
                continue;

            final String permission = permissionSupplier.annotation(PermissionSupplier.class).get().value();
            final PermissionSupplierData<T> data = new PermissionSupplierData<>((MethodContext<Boolean, T>) permissionSupplier, permission);
            permissionSupplierContext.register(type, data);
        }
    }

    private boolean isValidPermissionSupplier(final ApplicationContext context,
                                              final MethodContext<?, ?> permissionSupplier)
    {
        final List<TypeContext<?>> parameters = permissionSupplier.parameterTypes();
        if (parameters.size() != 2 || !parameters.get(0).is(Guild.class) || !parameters.get(1).is(Member.class)) {
            context.log().warn("The permission supplier %s must only have the parameters %s and %s"
                .formatted(permissionSupplier.qualifiedName(),
                    Guild.class.getCanonicalName(),
                    Member.class.getCanonicalName()));
            return false;
        }

        if (!permissionSupplier.returnType().is(boolean.class)) {
            context.log().warn("The permission supplier %s must return a boolean"
                .formatted(permissionSupplier.qualifiedName()));
            return false;
        }

        return true;
    }
}
