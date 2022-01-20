package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface DefaultTemporal {

    String format() default DEFAULT_FORMAT;
    String value() default "";

    static String DEFAULT_FORMAT = "##DEFAULT##";
}
