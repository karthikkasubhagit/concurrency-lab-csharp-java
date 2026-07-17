package dev.karthik.concurrencylab;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class ThreadingDemo {
    private static final int UPPER_BOUND = 500_000;

    private ThreadingDemo() {
    }

    static void run() throws Exception {
        System.out.println("\n2) MULTITHREADING — split CPU work across cores");

        int processors = Runtime.getRuntime().availableProcessors();
        int partitionCount = Math.max(2, Math.min(processors, 4));
        List<NumberRange> ranges = partition(2, UPPER_BOUND, partitionCount);
        Set<Long> threadIds = ConcurrentHashMap.newKeySet();
        long startedAt = System.nanoTime();
        List<Future<Integer>> workers = new ArrayList<>();

        var factory = Thread.ofPlatform()
                .name("fraud-worker-", 1)
                .factory();

        try (var executor = Executors.newFixedThreadPool(partitionCount, factory)) {
            for (NumberRange range : ranges) {
                workers.add(executor.submit(() -> {
                    Thread thread = Thread.currentThread();
                    threadIds.add(thread.threadId());
                    System.out.printf(
                            "   %-15s checks %,7d..%,7d (virtual=%s)%n",
                            thread.getName(),
                            range.start(),
                            range.end(),
                            thread.isVirtual());
                    return countPrimes(range);
                }));
            }

            int total = workers.stream().mapToInt(ThreadingDemo::get).sum();
            long elapsed =
                    Duration.ofNanos(System.nanoTime() - startedAt).toMillis();

            System.out.printf(
                    "   Found %,d primes using %d platform thread(s) in %d ms.%n",
                    total,
                    threadIds.size(),
                    elapsed);
        }

        System.out.println(
                "   More threads than useful CPU cores usually add overhead, not speed.");
    }

    private static int get(Future<Integer> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Caller was interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Worker failed", exception.getCause());
        }
    }

    private static int countPrimes(NumberRange range) {
        int count = 0;

        for (int candidate = range.start(); candidate <= range.end(); candidate++) {
            if (isPrime(candidate)) {
                count++;
            }
        }

        return count;
    }

    private static boolean isPrime(int value) {
        if (value < 2) {
            return false;
        }

        if (value == 2) {
            return true;
        }

        if (value % 2 == 0) {
            return false;
        }

        int limit = (int) Math.sqrt(value);
        for (int divisor = 3; divisor <= limit; divisor += 2) {
            if (value % divisor == 0) {
                return false;
            }
        }

        return true;
    }

    private static List<NumberRange> partition(int start, int end, int count) {
        int size = (end - start + 1) / count;
        var ranges = new ArrayList<NumberRange>(count);

        for (int index = 0; index < count; index++) {
            int rangeStart = start + (index * size);
            int rangeEnd =
                    index == count - 1 ? end : rangeStart + size - 1;
            ranges.add(new NumberRange(rangeStart, rangeEnd));
        }

        return ranges;
    }

    private record NumberRange(int start, int end) {
    }
}

