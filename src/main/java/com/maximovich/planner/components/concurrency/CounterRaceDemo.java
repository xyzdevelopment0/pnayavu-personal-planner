package com.maximovich.planner.components.concurrency;

import com.maximovich.planner.dtos.async.CounterRaceResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class CounterRaceDemo {

    public CounterRaceResponse run(int threads, int incrementsPerThread) {
        PlainCounter plainCounter = new PlainCounter();
        AtomicInteger atomicCounter = new AtomicInteger();
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Runnable> jobs = new ArrayList<>();
        for (int thread = 0; thread < threads; thread++) {
            jobs.add(() -> {
                for (int increment = 0; increment < incrementsPerThread; increment++) {
                    plainCounter.increment();
                    atomicCounter.incrementAndGet();
                    synchronizedCounter.increment();
                }
            });
        }
        jobs.forEach(executorService::execute);
        executorService.shutdown();
        await(executorService);
        int expected = threads * incrementsPerThread;
        return new CounterRaceResponse(
            threads,
            incrementsPerThread,
            expected,
            plainCounter.value(),
            atomicCounter.get(),
            synchronizedCounter.value(),
            expected - plainCounter.value()
        );
    }

    private void await(ExecutorService executorService) {
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    private static final class PlainCounter {
        private int value;

        private void increment() {
            value++;
        }

        private int value() {
            return value;
        }
    }

    private static final class SynchronizedCounter {
        private int value;

        private synchronized void increment() {
            value++;
        }

        private synchronized int value() {
            return value;
        }
    }
}
