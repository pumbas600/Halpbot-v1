package net.pumbas.halpbot.processors.permissions;

import org.dockbox.hartshorn.util.reflect.MethodContext;

public record PermissionSupplierData<T>(MethodContext<Boolean, T> supplier, String permission) {

}
