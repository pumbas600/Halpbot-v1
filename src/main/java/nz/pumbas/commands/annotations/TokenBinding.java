package nz.pumbas.commands.annotations;

import java.lang.annotation.Annotation;

public @interface TokenBinding
{
    Class<? extends Annotation>[] bindings();
}
