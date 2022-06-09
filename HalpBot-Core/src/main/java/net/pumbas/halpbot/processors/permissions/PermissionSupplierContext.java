package net.pumbas.halpbot.processors.permissions;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import lombok.Getter;


@AutoCreating
public class PermissionSupplierContext extends DefaultContext {

    @Getter
    private final MultiMap<TypeContext<?>, PermissionSupplierData<?>> permissionSuppliers = new ArrayListMultiMap<>();

    /**
     * Registers a PermissionSupplier method so that it can be processed by the {@code PermissionService}. Note that the
     * permission supplier method should be validated prior to being registered.
     *
     * @param type
     *     The class that the permission supplier is defined within
     * @param supplier
     *     The {@code PermissionSupplierData} to be registered
     * @param <T>
     *     The type of the class the permission supplier is defined within
     *
     * @see net.pumbas.halpbot.permissions.PermissionSupplier
     * @see net.pumbas.halpbot.permissions.PermissionService
     */
    public <T> void register(final TypeContext<T> type, final PermissionSupplierData<T> supplier) {
        this.permissionSuppliers.put(type, supplier);
    }
}
