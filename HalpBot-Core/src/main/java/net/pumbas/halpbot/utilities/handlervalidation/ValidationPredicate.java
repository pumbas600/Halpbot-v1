package net.pumbas.halpbot.utilities.handlervalidation;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.AnnotatedMemberContext;

import java.util.function.BiPredicate;

public interface ValidationPredicate extends BiPredicate<ApplicationContext, AnnotatedMemberContext<?>> {

}
