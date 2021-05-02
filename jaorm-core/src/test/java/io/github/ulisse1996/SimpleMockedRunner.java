package io.github.ulisse1996;

public class SimpleMockedRunner extends MockedRunner {

    @Override
    public boolean isCompatible(Class<?> klass) {
        return false;
    }

    @Override
    public boolean isSimple() {
        return true;
    }
}
