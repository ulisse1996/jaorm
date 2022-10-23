package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;

public class LowerFunction extends StringFunction {

    public LowerFunction(Selectable<String> selectable) {
        super(selectable);
    }

    @Override
    protected String getFormat() {
        return "LOWER(%s)";
    }
}