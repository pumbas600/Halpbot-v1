package nz.pumbas.commands.validation;

public @interface Min {

    double value();
    boolean inclusive() default false;
}
