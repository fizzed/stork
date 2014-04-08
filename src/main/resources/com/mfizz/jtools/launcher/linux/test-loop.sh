#!/usr/bin/env sh

paths="/usr:/usr/local"

IFS=":"; for p in $paths; do
    IFS=":"; for b in "$p/*"; do
    echo "path: $p"
done
