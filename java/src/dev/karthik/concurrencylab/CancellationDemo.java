package dev.karthik.concurrencylab;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class CancellationDemo {
    private CancellationDemo() {
    }

    static void run() throws InterruptedException {
        System.out.println("\n3) CANCELLATION — request, observe, stop safely");

        try (var executor = Executors.newSingleThreadExecutor()) {
            Future<?> order = executor.submit(CancellationDemo::processOrder);

            Thread.sleep(Duration.ofMillis(650));
            boolean requestAccepted = order.cancel(true);
            System.out.printf(
                    "   Caller requested cancellation (accepted=%s).%n",
                    requestAccepted);

            try {
                order.get();
            } catch (CancellationException expected) {
                System.out.println(
                        "   Caller observed the expected cancelled outcome.");
            } catch (java.util.concurrent.ExecutionException unexpected) {
                throw new IllegalStateException(
                        "Order failed instead of cancelling",
                        unexpected.getCause());
            }
        }
    }

    private static void processOrder() {
        for (int step = 1; step <= 10; step++) {
            try {
                // Sleep responds to Future.cancel(true) by throwing on interruption.
                Thread.sleep(Duration.ofMillis(150));
            } catch (InterruptedException cancelled) {
                // Preserve the signal for any caller further up the stack.
                Thread.currentThread().interrupt();
                System.out.println(
                        "   Worker observed the interrupt and stopped safely.");
                return;
            }

            // CPU-bound loops should check this flag periodically themselves.
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("   Worker observed cancellation in CPU work.");
                return;
            }

            System.out.printf("   completed order step %d/10%n", step);
        }
    }
}

