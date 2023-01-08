package io.github.ulisse1996.jaorm.vendor.specific;

public interface NullSpecific extends Specific {

    NullSpecific NO_OP = () -> false;

    boolean isNullSetterStrict();
}
