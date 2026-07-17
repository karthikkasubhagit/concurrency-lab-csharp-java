package dev.karthik.concurrencylab;

import java.util.Set;

public final class Main {
    private static final Set<String> VALID_MODES =
            Set.of("all", "tasking", "threading", "cancellation");

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "all" : args[0].toLowerCase();

        if (!VALID_MODES.contains(mode)) {
            System.err.println(
                    "Usage: ./run.sh [all|tasking|threading|cancellation]");
            System.exit(1);
        }

        System.out.println("JAVA CONCURRENCY LAB");
        System.out.printf(
                "Logical processors: %d%n",
                Runtime.getRuntime().availableProcessors());

        if (mode.equals("all") || mode.equals("tasking")) {
            TaskingDemo.run();
        }

        if (mode.equals("all") || mode.equals("threading")) {
            ThreadingDemo.run();
        }

        if (mode.equals("all") || mode.equals("cancellation")) {
            CancellationDemo.run();
        }
    }
}

