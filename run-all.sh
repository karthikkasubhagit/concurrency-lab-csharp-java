#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

dotnet run --project csharp/ConcurrencyLab -- "${1:-all}"
./java/run.sh "${1:-all}"
