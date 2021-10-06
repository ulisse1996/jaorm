package io.github.ulisse1996.jaorm.vendor.specific;

public interface LikeSpecific extends Specific {

    enum LikeType {
        FULL, START, END
    }

    String convertToLikeSupport(LikeType type, boolean caseInsensitive);

}
