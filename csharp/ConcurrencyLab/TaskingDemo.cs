using System.Diagnostics;

namespace ConcurrencyLab;

internal static class TaskingDemo
{
    public static async Task RunAsync()
    {
        Console.WriteLine("\n1) TASK-BASED CONCURRENCY — overlap time spent waiting");
        var stopwatch = Stopwatch.StartNew();

        Task<ServiceResult>[] calls =
        [
            CallServiceAsync("inventory", 450),
            CallServiceAsync("payment", 650),
            CallServiceAsync("shipping", 350)
        ];

        ServiceResult[] results = await Task.WhenAll(calls);

        Console.WriteLine(
            $"   All {results.Length} calls completed in ~{stopwatch.ElapsedMilliseconds} ms, " +
            "not their ~1,450 ms sum.");
        Console.WriteLine("   A Task represents work; it does not promise a dedicated thread.");
    }

    private static async Task<ServiceResult> CallServiceAsync(
        string name,
        int delayMilliseconds)
    {
        int startedOn = Environment.CurrentManagedThreadId;
        Console.WriteLine($"   {name,-9} started  on managed thread {startedOn}");

        // A timer represents non-blocking network I/O: no worker thread waits here.
        await Task.Delay(delayMilliseconds);

        int completedOn = Environment.CurrentManagedThreadId;
        Console.WriteLine($"   {name,-9} resumed  on managed thread {completedOn}");
        return new ServiceResult(name, delayMilliseconds);
    }

    private sealed record ServiceResult(string Name, int DurationMilliseconds);
}

