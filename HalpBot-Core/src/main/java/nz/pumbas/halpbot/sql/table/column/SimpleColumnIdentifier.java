/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package nz.pumbas.halpbot.sql.table.column;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.persistence.table.ColumnIdentifier;

import java.util.Objects;

//TODO: Remove
public class SimpleColumnIdentifier<T> implements ColumnIdentifier<T>
{

    private final String name;
    private final TypeContext<T> type;

    public SimpleColumnIdentifier(String name, Class<T> type) {
        this.name = name;
        this.type = TypeContext.of(type);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.type());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleColumnIdentifier<?>)) return false;
        SimpleColumnIdentifier<?> that = (SimpleColumnIdentifier<?>) o;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.type(), that.type());
    }

    @Override
    public String toString() {
        return "SimpleColumnIdentifier{" + "fieldName='" + this.name + '\'' + ", type=" + this.type + '}';
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public TypeContext<T> type() {
        return this.type;
    }
}
