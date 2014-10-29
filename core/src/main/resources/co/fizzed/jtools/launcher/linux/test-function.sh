#!/usr/bin/env sh

. $(dirname $0)/script-functions.sh

# test quietWhich
sh_bin=`quietWhich "sh"`
echo "location of sh: $sh_bin"

blah_bin=`quietWhich "doesnotexist"`
echo "location of something that does not exist: $blah_bin"

echo "finding all java binaries on system..."
java_cmds=`findJavaCommands`
echo "all java cmds: $java_cmds"

java_min_version="1.8"
extracted_min_version=`extractPrimaryJavaVersion "$java_min_version"`
echo "extracted min version from $java_min_version: $extracted_min_version"

echo "finding all java binaries on system that are min $java_min_version..."
java_bin=`findMinJavaVersion "$java_min_version" "$java_cmds"`

if [ -z "$java_bin" ]; then
    echo "Unable to find Java runtime version >= $java_min_version"
    exit 1
fi

echo "found java: $java_bin"

plat=`platform`
echo "platform: $plat"

system_memory=`systemMemory`
echo "system_memory: $system_memory"
