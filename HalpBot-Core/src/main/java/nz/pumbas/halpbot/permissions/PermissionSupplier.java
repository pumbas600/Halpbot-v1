package nz.pumbas.halpbot.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this method is a permission supplier and can be used to validate whether a member has the specified
 * permission within the guild.
 *
 * The method annotated with this must return a boolean and must take in a guild and member as parameters respectively.
 * E.g:
 * <pre>{@code
 *  PermissionSupplier("halpbot.example.demo")
 *  public boolean hasPermission(Guild guild, Member member) {
 *      // Some logic to determine if they have the permission
 *      return member.getIdLong() % 2 == 0;
 *  }
 * }</pre>
 * Note that the '@' is missing from the annotation due to limitations with javadoc rendering.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionSupplier
{
    /**
     * @return The permission being supplied
     */
    String value();
}
