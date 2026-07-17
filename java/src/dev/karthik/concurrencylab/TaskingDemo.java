package dev.karthik.concurrencylab;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class TaskingDemo {
    private TaskingDemo() {
    }

    static void run() throws ExecutionException, InterruptedException {
        System.out.println(
                "\n1) TASK-BASED CONCURRENCY — overlap time spent waiting");
        long startedAt = System.nanoTime();
        List<ServiceCall> services = List.of(
                new ServiceCall("inventory", Duration.ofMillis(450)),
                new ServiceCall("payment", Duration.ofMillis(650)),
                new ServiceCall("shipping", Duration.ofMillis(350)));

        List<ServiceResult> results;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ServiceResult>> calls = services.stream()
                    .map(service -> executor.submit(() -> callService(service)))
                    .toList();

            results = calls.stream().map(TaskingDemo::get).toList();
        }

        long elapsed = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        System.out.printf(
                "   All %d calls completed in ~%d ms, not their ~1,450 ms sum.%n",
                results.size(),
                elapsed);
        System.out.println(
                "   Each task used a virtual thread; the JVM maps them to carrier threads.");
    }

    private static ServiceResult callService(ServiceCall service)
            throws InterruptedException {
        Thread thread = Thread.currentThread();
        System.out.printf(
                "   %-9s started  on %s (virtual=%s)%n",
                service.name(),
                thread.threadId(),
                thread.isVirtual());

        // Supported blocking operations unmount a virtual thread from its carrier.
        Thread.sleep(service.duration());

        System.out.printf(
                "   %-9s resumed  on %s (virtual=%s)%n",
                service.name(),
                thread.threadId(),
                thread.isVirtual());
        return new ServiceResult(service.name(), service.duration());
    }

    private static ServiceResult get(Future<ServiceResult> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Caller was interrupted", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Service call failed", exception.getCause());
        }
    }

    private record ServiceCall(String name, Duration duration) {
    }

    private record ServiceResult(String name, Duration duration) {
    }
}
