package org.excelsi.nausicaa.ca;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;


public class ThisThreadExecutorService implements ExecutorService {
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        throw new UnsupportedOperationException();
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
        throw new UnsupportedOperationException();
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    public boolean isShutdown() {
        return true;
    }

    public boolean isTerminated() {
        return true;
    }

    public void shutdown() {
    }

    public List<Runnable> shutdownNow() {
        return Collections.<Runnable>emptyList();
    }

    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> f = new FutureTask<>(task);
        f.run();
        return f;
    }

    public Future<?> submit(Runnable task) {
        FutureTask<?> f = new FutureTask<>(task, null);
        f.run();
        return f;
    }

    public void execute(Runnable r) {
        r.run();
    }

    public <T> Future<T> submit(Runnable task, T result) {
        FutureTask<T> f = new FutureTask<>(task, result);
        f.run();
        return f;
    }
}
