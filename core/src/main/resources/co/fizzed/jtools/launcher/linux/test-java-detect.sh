#!/usr/bin/env sh

. $(dirname $0)/script-java-detect-header.sh
. $(dirname $0)/script-functions.sh

all_java_bins=`findAllJavaExecutables`
echo "all_java_bins: $all_java_bins"

java_bin_6=`findFirstJavaExecutableByMinimumMajorVersion "$all_java_bins" "1.6"`
echo "java_bin_6: $java_bin_6"

java_bin_7=`findFirstJavaExecutableByMinimumMajorVersion "$all_java_bins" "1.7"`
echo "java_bin_7: $java_bin_7"

java_bin_8=`findFirstJavaExecutableByMinimumMajorVersion "$all_java_bins" "1.8"`
echo "java_bin_8: $java_bin_8"

java_bin_latest_maj=`findLatestJavaExecutableByMajorVersion "$all_java_bins"`
echo "java_bin_latest_maj: $java_bin_latest_maj"
