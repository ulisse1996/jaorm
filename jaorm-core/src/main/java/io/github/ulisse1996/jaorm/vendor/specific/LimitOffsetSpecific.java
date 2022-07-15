package io.github.ulisse1996.jaorm.vendor.specific;

public interface LimitOffsetSpecific extends Specific {

    String convertOffSetLimitSupport(int limitRow);
    String convertOffsetSupport(int offset);
    String convertOffSetLimitSupport(int limitRow, int offsetRow);
    default boolean requiredOrder() {
        return false;
    }
}
