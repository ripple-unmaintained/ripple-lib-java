package com.ripple.client.async;

abstract public class ComposedOperation<T1, T2, T3, T4> {
    public T1 first;
    public T2 second;
    public T3 third;
    public T4 fourth;

    protected abstract void finished();
    protected abstract int numOps();

    int done = 0;

    private void doneOne() {
        if (++done == numOps()) {
            finished();
        }
    }
    public void first(T1 pfirst) {
        first = pfirst;
        doneOne();
    }

    public void second(T2 psecond) {
        second = psecond;
        doneOne();
    }

    public void third(T3 pthird) {
        third = pthird;
        doneOne();
    }

    public void fourth(T4 pfourth) {
        fourth = pfourth;
        doneOne();
    }
}
