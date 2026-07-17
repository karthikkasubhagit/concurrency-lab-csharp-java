namespace ConcurrencyLab;

internal static class CancellationDemo
{
    public static async Task RunAsync()
    {
        Console.WriteLine("\n3) CANCELLATION — request, observe, stop safely");

        using var cancellationSource =
            new CancellationTokenSource(TimeSpan.FromMilliseconds(650));

        try
        {
            await ProcessOrderAsync(cancellationSource.Token);
        }
        catch (OperationCanceledException)
            when (cancellationSource.IsCancellationRequested)
        {
            Console.WriteLine("   Caller observed the expected cancelled outcome.");
        }
    }

    private static async Task ProcessOrderAsync(CancellationToken cancellationToken)
    {
        for (int step = 1; step <= 10; step++)
        {
            // Real APIs such as HttpClient and EF Core also accept the token.
            await Task.Delay(150, cancellationToken);

            // Useful in CPU loops; it throws OperationCanceledException when signalled.
            cancellationToken.ThrowIfCancellationRequested();
            Console.WriteLine($"   completed order step {step}/10");
        }
    }
}

