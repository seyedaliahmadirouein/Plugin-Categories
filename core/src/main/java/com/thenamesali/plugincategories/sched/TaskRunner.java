package com.thenamesali.plugincategories.sched;

public interface TaskRunner {

    void runAsync(Runnable task);

    void runGlobal(Runnable task);

    void runGlobalDelayed(long delayTicks, Runnable task);
}
