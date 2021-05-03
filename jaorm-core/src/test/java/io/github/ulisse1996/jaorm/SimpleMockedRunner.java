package io.github.ulisse1996.jaorm;

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
