package engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods or classes that should be excluded from JaCoCo coverage
 * reports. JaCoCo automatically ignores any code annotated with an annotation whose simple name is
 * "Generated". The retention policy must be CLASS or RUNTIME.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Generated {}
