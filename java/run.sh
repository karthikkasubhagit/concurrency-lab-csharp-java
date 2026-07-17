#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

rm -rf out
mkdir -p out

javac --release 21 -d out $(find src -name '*.java' -print)
java -cp out dev.karthik.concurrencylab.Main "${1:-all}"
