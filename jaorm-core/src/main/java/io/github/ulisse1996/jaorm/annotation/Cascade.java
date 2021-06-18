package io.github.ulisse1996.jaorm.annotation;

import java.lang.annotation.*;

/**
 * Specifies that linked Entity must also receive the {@link io.github.ulisse1996.jaorm.entity.relationship.EntityEventType
 * EntityEventType} from the main Entity
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@Documented
public @interface Cascade {

    CascadeType[] value();
}
