
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

getOperatingSystemName()
{
    local u=$(uname)
    if [ "$u" = "Linux" ]; then
      echo "linux"
    elif [ "$u" = "FreeBSD" ]; then
      echo "freebsd"
    elif [ "$u" = "OpenBSD" ]; then
      echo "openbsd"
    elif [ "$u" = "Darwin" ]; then
      echo "osx"
    else
      echo "unknown"
    fi
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

getSystemMemoryMB()
{
  # osx: top -l 1 | awk '/PhysMem:/ {print $10}' -> 8006M
  # linux: grep MemTotal /proc/meminfo | awk '{print $2}' -> 32930344 (as KB)
  if [ -f /proc/meminfo ]; then
    #echo "on linux.."
    TMPMEMKB=`grep MemTotal /proc/meminfo | awk '{print $2}'`
    TMPMEM=$(expr $TMPMEMKB / 1000)
    echo $TMPMEM
    return 0
  fi

  if isOperatingSystemOSX; then
    TMPMEMBYTES=`sysctl -a | grep "hw.physmem" | awk '/ = / {print $3}'`
    # hw.physmem = 2147483648
    # convert into MB
    TMPMEM=$(expr $TMPMEMBYTES / 1000 / 1000)
    echo $TMPMEM
    return 0
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

# JAVA_VERSION=`javaVersion $JAVA_HOME/jre/bin/java`
getJavaVersion()
{
  TMPJAVA=$1
  echo `expr "$($TMPJAVA -version 2>&1 | head -1)" : '.*version.*"\([0-9._]*\)"'`
}


# `appendPath $1 $2`
appendPath()
{
  if [ -z "$1" ]; then
    echo "$2"
  else
    echo "$1:$2"
  fi
}


# `findJavaCommands` -> returns string of paths separated by colon
# e.g. "/usr/bin/java:/usr/lib/jvm/bin/java"
findJavaCommands()
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
    if [ ! -z $which_java ]; then
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
    
    java_home_parents="/usr/lib/jvm:/usr/java"

    if isOperatingSystemOSX; then
        java_home_parents=`appendPath "$java_home_parents" "/Library/Internet Plug-Ins:/System/Library/Frameworks/JavaVM.framework/Versions:/Library/Java/JavaVirtualMachines:/System/Library/Java/JavaVirtualMachines"`
    fi
    
    logJavaSearchDebug "trying well-known java homes..."
    local IFS=":"
    for java_home_parent in $java_home_parents; do
        #echo "searching java_home_parent: $java_home_parent"
	for maybe_java_home in $java_home_parent/*; do
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

            # osx path
            local osx_jre_bin="$maybe_java_home/Contents/Home/jre/bin/java"
            local osx_jdk_bin="$maybe_java_home/Contents/Home/bin/java"
            if [ -x "$osx_jre_bin" ]; then
                logJavaSearchDebug "found $osx_jre_bin"
                java_cmds=`appendPath "$java_cmds" "$osx_jre_bin"`
            elif [ -x "$osx_jdk_bin" ]; then
                logJavaSearchDebug "found $osx_jdk_bin"
                java_cmds=`appendPath "$java_cmds" "$osx_jdk_bin"`
            fi
        done
    done

    echo "$java_cmds"
}

# java_bin=`extractMajorJavaVersion 1.7`
# returns: "7"
extractPrimaryJavaVersion()
{
    local min_version="$1"
    
    # "1.7" -> extract java version
    local target_java_version=`echo $min_version | awk '{split($0, array, ".")} END{print array[2]}'`

    echo $target_java_version
}


# java_bin=`findMinJavaVersion 1.7 <java_cmds separated by colon>`
findMinJavaVersion()
{
    local min_version="$1"
    local java_cmds_line="$2"
    
    # "1.7" -> extract java version
    local target_java_version=`extractPrimaryJavaVersion "$min_version"`

    local IFS=":"
    for java_cmd in $java_cmds_line; do
        #echo "getting version from: $java_cmd"
        java_version=`"$java_cmd" -version 2>&1 | grep "version" | awk '{print $3}' | tr -d \" | awk '{split($0, array, ".")} END{print array[2]}'`
        #echo "java_version: $java_cmd -> $java_version"
        if [ "$java_version" != "" ] && [ $java_version -ge $target_java_version ]; then
             echo $java_cmd
             return 1
        fi
    done

    return 0
}

# JAVA_CLASSPATH=`buildJavaClasspath $jarDir`
buildJavaClasspath()
{
  # path to main application directory
  TMPAPPDIR=$1

  if [ -d $TMPAPPDIR ]; then
    for file in $TMPAPPDIR/*.jar; do
      TMPCLASSPATH=`appendPath "$TMPCLASSPATH" "$file"`
    done
  fi

  echo $TMPCLASSPATH
}

# verifyNotRunning $JAVA_PID
verifyNotRunning()
{
  TMPPID=$1
  if [ -f $TMPPID ]
  then
    if running $TMPPID
    then
      echo "Already running!!"
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
    echo "Warning: app is not running!"
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
      echo "Unable to kill app within timeout; may need to kill it manually [pid: $TMPPID]"
  else
      rm -f $TMPPID
  fi
}
