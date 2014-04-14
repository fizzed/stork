
#
# handle options
#

doAllJavaExecutables()
{
    local all_java_bins=`findAllJavaExecutables`
    if [ -z $all_java_bins ]; then
        echo "No java executables found"
        exit 2
    else
        echo "$all_java_bins"
    fi
}

doFirstMinVersion()
{
    local all_java_bins=`findAllJavaExecutables`
    local min_version="$1"
    if [ -z $min_version ]; then
        echo "parameter 2 required to be minimum java version (e.g. 1.7)"
        exit 1
    fi

    local java_bin=`findFirstJavaExecutableByMinimumMajorVersion "$all_java_bins" "$min_version"`
    if [ -z $java_bin ]; then
        echo "No java executables found"
        exit 2
    else
        echo "$java_bin"
    fi
}

doLatestMajorVersion()
{
    local all_java_bins=`findAllJavaExecutables`
    local java_bin=`findLatestJavaExecutableByMajorVersion "$all_java_bins"`
    if [ -z $java_bin ]; then
        echo "No java executables found"
        exit 2
    else
        echo "$java_bin"
    fi
}

doLatestMajorVersionNumber()
{
    local java_bin=`doLatestMajorVersion`
    if [ -z "$java_bin" ]; then
        echo "No java executables found"
        exit 100
    else
        java_ver=`getJavaVersion "$java_bin"`
        java_maj_ver=`parseJavaMajorVersion "$java_ver"`
        echo $java_maj_ver
        exit $java_maj_ver
    fi
}

case "$1" in

  "--all-java-executables")
    doAllJavaExecutables
    ;;

  "--first-min-version")
    doFirstMinVersion $2
    ;;

  "--latest-major-version")
    doLatestMajorVersion
    ;;

  "--latest-major-version-number")
    doLatestMajorVersionNumber
    ;;

  *)
    echo "Usage: $0 " >&2
    exit 1
    ;;

esac

exit 0