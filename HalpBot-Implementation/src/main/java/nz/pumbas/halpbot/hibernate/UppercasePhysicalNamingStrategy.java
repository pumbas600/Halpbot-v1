package nz.pumbas.halpbot.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

public class UppercasePhysicalNamingStrategy implements PhysicalNamingStrategy
{
    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.convertToUppercase(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.convertToUppercase(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.convertToUppercase(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.convertToUppercase(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.convertToUppercase(name);
    }

    private Identifier convertToUppercase(Identifier identifier) {
        if (null == identifier) return null;

        final String newIdentifier = identifier.getText().toUpperCase(Locale.ROOT);
        return Identifier.toIdentifier(newIdentifier);
    }
}
