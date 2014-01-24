package com.ripple.client.executors;

public interface ClientExecutor {
    public void run(Runnable runnable);
    public void runDelayed(Runnable runnable, int ms);
}
