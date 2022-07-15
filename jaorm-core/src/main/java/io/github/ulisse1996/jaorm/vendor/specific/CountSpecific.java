package io.github.ulisse1996.jaorm.vendor.specific;

public interface CountSpecific extends Specific {

    CountSpecific NO_OP = () -> false;

    boolean isNamedCountRequired();
}
