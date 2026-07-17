namespace ConcurrencyLab;

internal static class Program
{
    private static readonly HashSet<string> ValidModes =
        ["all", "tasking", "threading", "cancellation"];

    public static async Task<int> Main(string[] args)
    {
        string mode = args.FirstOrDefault()?.ToLowerInvariant() ?? "all";

        if (!ValidModes.Contains(mode))
        {
            Console.Error.WriteLine(
                "Usage: dotnet run -- [all|tasking|threading|cancellation]");
            return 1;
        }

        Console.WriteLine(".NET CONCURRENCY LAB");
        Console.WriteLine($"Logical processors: {Environment.ProcessorCount}");

        if (mode is "all" or "tasking")
        {
            await TaskingDemo.RunAsync();
        }

        if (mode is "all" or "threading")
        {
            await ThreadingDemo.RunAsync();
        }

        if (mode is "all" or "cancellation")
        {
            await CancellationDemo.RunAsync();
        }

        return 0;
    }
}

