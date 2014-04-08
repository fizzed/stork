#!/usr/bin/env sh

paths="/usr:/usr/local"

IFS=":"
for p in $paths; do
    echo "path: $p"
done
