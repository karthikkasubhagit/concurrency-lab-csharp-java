using System.Collections.Concurrent;
using System.Diagnostics;

namespace ConcurrencyLab;

internal static class ThreadingDemo
{
    private const int UpperBound = 500_000;

    public static async Task RunAsync()
    {
        Console.WriteLine("\n2) MULTITHREADING — split CPU work across cores");

        int partitionCount = Math.Clamp(Environment.ProcessorCount, 2, 4);
        IReadOnlyList<NumberRange> ranges = Partition(2, UpperBound, partitionCount);
        var threadIds = new ConcurrentDictionary<int, byte>();
        var stopwatch = Stopwatch.StartNew();

        Task<int>[] workers = ranges
            .Select(range => Task.Run(() =>
            {
                int threadId = Environment.CurrentManagedThreadId;
                threadIds.TryAdd(threadId, 0);
                Console.WriteLine(
                    $"   thread {threadId,2} checks {range.Start,7:N0}..{range.End,7:N0}");
                return CountPrimes(range);
            }))
            .ToArray();

        int total = (await Task.WhenAll(workers)).Sum();

        Console.WriteLine(
            $"   Found {total:N0} primes using {threadIds.Count} thread-pool thread(s) " +
            $"in {stopwatch.ElapsedMilliseconds} ms.");
        Console.WriteLine("   More threads than useful CPU cores usually add overhead, not speed.");
    }

    private static int CountPrimes(NumberRange range)
    {
        int count = 0;

        for (int candidate = range.Start; candidate <= range.End; candidate++)
        {
            if (IsPrime(candidate))
            {
                count++;
            }
        }

        return count;
    }

    private static bool IsPrime(int value)
    {
        if (value < 2)
        {
            return false;
        }

        if (value == 2)
        {
            return true;
        }

        if (value % 2 == 0)
        {
            return false;
        }

        int limit = (int)Math.Sqrt(value);
        for (int divisor = 3; divisor <= limit; divisor += 2)
        {
            if (value % divisor == 0)
            {
                return false;
            }
        }

        return true;
    }

    private static IReadOnlyList<NumberRange> Partition(
        int start,
        int end,
        int count)
    {
        int size = (end - start + 1) / count;

        return Enumerable.Range(0, count)
            .Select(index =>
            {
                int rangeStart = start + (index * size);
                int rangeEnd = index == count - 1 ? end : rangeStart + size - 1;
                return new NumberRange(rangeStart, rangeEnd);
            })
            .ToArray();
    }

    private sealed record NumberRange(int Start, int End);
}

