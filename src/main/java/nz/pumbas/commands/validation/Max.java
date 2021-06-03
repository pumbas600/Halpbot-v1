package nz.pumbas.commands.validation;

public @interface Max {

    double value();
    boolean isInclusive() default false;
}
