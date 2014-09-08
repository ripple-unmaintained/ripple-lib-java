package com.ripple.client.async;

abstract public class Composed3<T1, T2, T3> extends ComposedOperation<T1, T2, T3, Object> {
    @Override
    protected int numOps() {
        return 3;
    }

    @Override
    protected void finished() {
        on3(first, second, third);
    }

    abstract void on3(T1 first, T2 second, T3 third);
}
