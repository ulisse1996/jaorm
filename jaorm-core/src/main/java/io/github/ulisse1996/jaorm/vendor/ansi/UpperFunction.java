package io.github.ulisse1996.jaorm.vendor.ansi;

import io.github.ulisse1996.jaorm.Selectable;

public class UpperFunction extends StringFunction {

    public UpperFunction(Selectable<String> selectable) {
        super(selectable);
    }

    @Override
    protected String getFormat() {
        return "UPPER(%s)";
    }
}