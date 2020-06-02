
#
# functions
#

logLauncherDebug()
{
    if [ "$LAUNCHER_DEBUG" = "1" ]; then
        echo "[LAUNCHER] $1" >&2
    fi
}

logJavaSearchDebug()
{
    if [ "$LAUNCHER_DEBUG" = "1" ]; then
        echo "[JAVA_SEARCH] $1" >&2
    fi
}

quietWhich()
{
    # $(which bad) results in output we want to ignore
    W=$(which $1 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo $W
    fi
}

# RUN_DIR=`findDirectory -w /var/run /usr/var/run /tmp`
findDirectory()
{
    OP=$1
    shift
    for L in $* ; do
        [ $OP $L ] || continue
        echo $L
        break
    done
}

getOperatingSystemName()
{
    local u=$(uname)
    case "$u" in
        Linux)
            echo "linux"
            ;;
        Darwin)
            echo "osx"
            ;;
        FreeBSD)
            echo "freebsd"
            ;;
        OpenBSD)
            echo "openbsd"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            echo "windows"
            ;;
        *)
            echo "unknown"
            ;;
    esac
}

isOperatingSystemOSX()
{
    local p=`getOperatingSystemName`
    if [ "$p" = "osx" ]; then
      return 0
    else
      return 1
    fi
}

isOperatingSystemLinux()
{
    local p=`getOperatingSystemName`
    if [ "$p" = "linux" ]; then
        return 0
    else
        return 1
    fi
}

isOperatingSystemFreeBSD()
{
    local p=`getOperatingSystemName`
    if [ "$p" = "freebsd" ]; then
        return 0
    else
        return 1
    fi
}

isOperatingSystemOpenBSD()
{
    local p=`getOperatingSystemName`
    if [ "$p" = "openbsd" ]; then
        return 0
    else
        return 1
    fi
}

isOperatingSystemWindows()
{
    local p=`getOperatingSystemName`
    if [ "$p" = "windows" ]; then
        return 0
    else
        return 1
    fi
}

getSystemMemoryMB()
{
    local mem_mb=""

    # linux: grep MemTotal /proc/meminfo | awk '{print $2}' -> 32930344 (as KB)
    if [ -f /proc/meminfo ]; then
        local mem_kb=`grep MemTotal /proc/meminfo | awk '{print $2}'`
        if [ ! -z $mem_kb ]; then
            # convert kilobytes to megabytes
            mem_mb=$(expr $mem_kb / 1024)
        fi
    fi

    # works on mac osx
    if [ -z $mem_mb ] && isOperatingSystemOSX; then
        local mem_bytes=`sysctl -a 2>/dev/null | grep "hw.memsize" | head -n 1 | awk -F'=' '{print $2}'`
        if [ ! -z $mem_bytes ]; then
            # convert bytes to megabytes
            mem_mb=$(expr $mem_bytes / 1024 / 1024)
        fi
    fi

    # try sysctl for hw.physmem works on freebsd/openbsd
    if [ -z $mem_mb ]; then
        local mem_bytes=`sysctl -a 2>/dev/null | grep "hw.physmem" | head -n 1 | awk -F'[:=]' '{print $2}'`
        if [ ! -z $mem_bytes ]; then
            # convert bytes to megabytes
            mem_mb=$(expr $mem_bytes / 1024 / 1024)
        fi
    fi

    if [ -z $mem_mb ]; then
        echo 0
    else
        echo $mem_mb
    fi
}

pctOf()
{
  num=$(($1 * $2))
  echo $(($num / 100))
}

readAllLinks()
{
  # prefer realpath
  if [ ! -z `quietWhich realpath` ]; then
    J=$(realpath $1)
  elif [ ! -z `quietWhich readlink` ]; then
    # fallback to manually reading symlinks
    J=$1
    while [ -L $J ]; do
      J=`readlink $J`
    done
  else
    J=$1
  fi
  echo $J
}


# appended_path=`appendPath $1 $2`
appendPath()
{
  if [ -z "$1" ]; then
    echo "$2"
  else
    echo "$1:$2"
  fi
}


# JAVA_VERSION=`javaVersion "$JAVA_HOME/jre/bin/java"`
getJavaVersion()
{
    local java_bin="$1"
    #logJavaSearchDebug "getJavaVersion from: $java_bin"
    #local java_ver_line=`"$java_bin" -version 2>&1 | head -1`
    #logJavaSearchDebug "getJavaVersion ver line: $java_ver_line"
    #echo `expr "'$java_ver_line'" : '.*version.*"\(.*\)"'`
    # extracts 1.8.0_144 or 9.0.1
    local java_ver=`"$java_bin" -version 2>&1 | grep "version" | awk '{print $3}' | tr -d \"`
    echo "$java_ver"
}


# $java_bins=`findAllJavaExecutables`
# returns: all java executables separated by colon "/usr/bin/java:/usr/lib/jvm/bin/java"
findAllJavaExecutables()
{
    local java_cmds=""

    # is JAVA_HOME set?
    logJavaSearchDebug "searching JAVA_HOME..."
    if [ ! -z "$JAVA_HOME" ]; then
        local jre_bin="$JAVA_HOME/jre/bin/java"
        local jdk_bin="$JAVA_HOME/bin/java"
        if [ -x "$jre_bin" ]; then
            logJavaSearchDebug "found $jre_bin"
            java_cmds=`appendPath "$java_cmds" "$jre_bin"`
        fi
        if [ -x "$jdk_bin" ]; then
            logJavaSearchDebug "found $jdk_bin"
            java_cmds=`appendPath "$java_cmds" "$jdk_bin"`
        fi
    fi;

    # is java in path
    logJavaSearchDebug "searching PATH..."
    local which_java=`quietWhich java`
    if [ ! -z "$which_java" ]; then
        if [ -x "$which_java" ]; then
            logJavaSearchDebug "found $which_java"
            java_cmds=`appendPath "$java_cmds" "$which_java"`
        fi
    fi

    # special case on mac os x
    if isOperatingSystemOSX; then
        local osx_java_home=""
        logJavaSearchDebug "trying /usr/libexec/java_home..."
        if [ -x '/usr/libexec/java_home' ]; then
            osx_java_home=`/usr/libexec/java_home`
        fi
        if [ ! -z $osx_java_home ]; then
            if [ -x "$osx_java_home/bin" ]; then
                logJavaSearchDebug "found $osx_java_home/bin"
                java_cmds=`appendPath "$java_cmds" "$osx_java_home/bin"`
            fi
        fi
    fi

    # search all known java home locations for java binaries
    # linux openjdk: /usr/lib/jvm
    # centos/redhat sunjdk: /usr/java
    
    local java_home_parents=""

    # common install dir on linux
    java_home_parents=`appendPath "$java_home_parents" "/usr/lib/jvm/*"`
    java_home_parents=`appendPath "$java_home_parents" "/usr/java/*"`
    
    # common install dir on freebsd
    java_home_parents=`appendPath "$java_home_parents" "/usr/local/openjdk*"`

    # common install dir on openbsd
    java_home_parents=`appendPath "$java_home_parents" "/usr/local/jdk*"`
    java_home_parents=`appendPath "$java_home_parents" "/usr/local/jre*"`
    
    if isOperatingSystemOSX; then
        java_home_parents=`appendPath "$java_home_parents" "/Library/Internet Plug-Ins/Java*/Contents/Home"`
        #java_home_parents=`appendPath "$java_home_parents" "/System/Library/Frameworks/JavaVM.framework/Versions/*"`
        java_home_parents=`appendPath "$java_home_parents" "/Library/Java/JavaVirtualMachines/*/Contents/Home"`
        java_home_parents=`appendPath "$java_home_parents" "/System/Library/Java/JavaVirtualMachines/*/Contents/Home"`
    fi

    if isOperatingSystemWindows; then
        java_home_parents=`appendPath "$java_home_parents" "/c/Program Files/Java/*"`
    fi
    
    logJavaSearchDebug "trying well-known java homes..."
    local IFS=":"
    for java_home_parent in $java_home_parents; do
        #echo "searching java_home_parent: $java_home_parent"
	for maybe_java_home in $java_home_parent; do
            [ -d "$maybe_java_home" ] || continue   

            local jre_bin="$maybe_java_home/jre/bin/java"
            local jdk_bin="$maybe_java_home/bin/java"

            if [ -x "$jre_bin" ]; then
                logJavaSearchDebug "found $jre_bin"
                java_cmds=`appendPath "$java_cmds" "$jre_bin"`
            elif [ -x "$jdk_bin" ]; then
                logJavaSearchDebug "found $jdk_bin"
                java_cmds=`appendPath "$java_cmds" "$jdk_bin"`
            fi
        done
    done

    echo "$java_cmds"
}

# java_maj_ver=`parseJavaMajorVersion 1.7`
# returns: "7"
parseJavaMajorVersion()
{
    local version="$1"
    local major=`echo "$version" | awk '{split($0, array, ".")} END{print array[1]}'`
    local minor=`echo "$version" | awk '{split($0, array, ".")} END{print array[2]}'`
    local patch=`echo "$version" | awk '{split($0, array, ".")} END{print array[3]}'`

    # if major > 1 then it represents the major java version (e.g. Java 9+)
    # we want 1.8.0_44 to return 8, 9.0.1 to return 9
    if [ "x$major" = "x" ]; then
        return 0
    elif [ "$major" -gt "1" ]; then
        echo $major
    else
        echo $minor
    fi
}


# java_bin=`findFirstJavaForMinimumMajorVersion "<java_cmds separated by colon>" "1.7"`
findFirstJavaExecutableByMinimumMajorVersion()
{
    local java_bins="$1"
    local min_java_ver="$2"
    local max_java_ver="$3"
    local target_min_java_maj_ver=`parseJavaMajorVersion "$min_java_ver"`
    local target_max_java_maj_ver=`parseJavaMajorVersion "$max_java_ver"`

    local IFS=":"
    for java_bin in $java_bins; do
        java_ver=`getJavaVersion "$java_bin"`
        logJavaSearchDebug "evaluting if $java_bin with version $java_ver >= 1.$target_min_java_maj_ver"
        java_maj_ver=`parseJavaMajorVersion "$java_ver"`
        if [ "$java_maj_ver" != "" ] && [ $java_maj_ver -ge $target_min_java_maj_ver ]; then
            if [ -z "$max_java_ver" ] || [ $java_maj_ver -le $target_max_java_maj_ver ]; then
                echo "$java_bin"
                return 1
            fi
        fi
    done
    return 0
}


# java_bin=`findLatestJavaExecutableByMajorVersion "<java_cmds separated by colon>"`
findLatestJavaExecutableByMajorVersion()
{
    local java_bins="$1"
    local latest_java_maj_ver=0
    local latest_java_bin=""

    local IFS=":"
    for java_bin in $java_bins; do
        java_ver=`getJavaVersion "$java_bin"`
        logJavaSearchDebug "evaluting if $java_bin is a new major java version on system"
        java_maj_ver=`parseJavaMajorVersion "$java_ver"`
        if [ "$java_maj_ver" != "" ] && [ $java_maj_ver -gt $latest_java_maj_ver ]; then
             latest_java_maj_ver=$java_maj_ver
             latest_java_bin=$java_bin
        fi
    done

    echo "$latest_java_bin"
}


# JAVA_CLASSPATH=`buildJavaClasspath $jarDir`
buildJavaClasspath()
{
  # path to main application directory
  TMPAPPDIR="$1"

  if [ -d "$TMPAPPDIR" ]; then
    for file in "$TMPAPPDIR"/*.jar; do
      TMPCLASSPATH=`appendPath "$TMPCLASSPATH" "$file"`
    done
  fi

  echo $TMPCLASSPATH
}


# if running $PID_FILE; then
#   echo "running..."
# fi
running()
{
    local PID=$(cat "$1" 2>/dev/null) || return 1
    #[ -f $1 ] || return 1
    #PID=$(cat $1)
    ps -p $PID >/dev/null 2>/dev/null || return 1
    #return 0
    # kill -0 does not work if the daemon was started with a different user
    #kill -0 "$PID" 2>/dev/null
}

# verifyNotRunning $JAVA_PID
verifyNotRunning()
{
  TMPPID=$1
  if [ -f $TMPPID ]
  then
    if running $TMPPID
    then
      PID=`cat $TMPPID 2>/dev/null`
      echo "$NAME is currently running with pid $PID"
      exit 1
    else
      # dead pid file - remove
      rm -f $TMPPID
    fi
  fi
}

# checkRunning $STRATUS_PID
checkRunning()
{
  TMPPID=$1
  if [ -f $TMPPID ]
  then
    if ! running $TMPPID
    then
      echo "Warning: app is not running!"
      # dead pid file - remove
      rm -f $TMPPID
    fi
  else
    echo "$NAME is not currently running!"
  fi
}


stopJavaApp()
{
  TMPPID=$1
  PID=`cat $TMPPID 2>/dev/null`
  local timeout=60
  while running $TMPPID && [ $timeout -gt 0 ]; do
    kill $PID 2>/dev/null
    sleep 1
    printf "."
    timeout=`expr $timeout - 1`
  done
  if [ ! $timeout -gt 0 ]
  then 
      echo "Unable to kill $NAME within timeout; may need to kill it manually [pid: $TMPPID]"
  else
      rm -f $TMPPID
  fi
}
