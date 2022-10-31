package io.github.ulisse1996.jaorm.dsl.query.impl;

import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LimitOffsetSpecific;

public abstract class AbstractLimitOffsetImpl {

    protected void buildLimitOffset(StringBuilder builder, int limit, int offset) {
        if (offset > 0 && limit > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffSetLimitSupport(limit, offset));
        } else if (limit > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffSetLimitSupport(limit));
        } else if (offset > 0) {
            builder.append(VendorSpecific.getSpecific(LimitOffsetSpecific.class).convertOffsetSupport(offset));
        }
    }
}
