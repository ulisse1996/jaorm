package io.github.ulisse1996.jaorm.spi.common;

class SingletonImpl<T> implements Singleton<T> {

    private T val;

    public SingletonImpl(T val) {
        this.val = val;
    }

    @Override
    public T get() {
        return this.val;
    }

    @Override
    public void set(T val) {
        this.val = val;
    }

    @Override
    public boolean isPresent() {
        return this.val != null;
    }
}
