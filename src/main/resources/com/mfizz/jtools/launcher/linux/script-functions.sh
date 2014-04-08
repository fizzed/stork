
#
# functions
#

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

isOSX()
{
    local p=`platform`
    if [ "$p" = "osx" ]; then
      return 0
    else
      return 1
    fi
}

isLinux()
{
    local p=`platform`
    if [ "$p" = "linux" ]; then
        return 0
    else
        return 1
    fi
}

platform()
{
    local u=$(uname)
    if [ "$u" = "Linux" ]; then
      echo "linux"
    elif [ "$u" = "Darwin" ]; then
      echo "osx"
    else
      echo "unknown"
    fi
}

systemMemory()
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

  if isOSX; then
    # this method failed on some versions of osx
    #TMPMEM=`top -l 1 | awk '/PhysMem:/ {print $10}'`
    # strip off last M
    #TMPMEMMB=${TMPMEM%%M}
    #echo $TMPMEMMB
    #return 0

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

# JAVA_CMD=`findJavaCommand`
findJavaCommand()
{
  if [ -z "$JAVA" ]; then
    TMPJAVACMD=`quietWhich java`
    if [ ! -z $TMPJAVACMD ]; then
      # osx does not have -f, manually do search
      #TMPJAVACMD=$(readlink -f $TMPJAVACMD)
      TMPJAVACMD=`readAllLinks $TMPJAVACMD`
      echo $TMPJAVACMD
    fi
  else
    echo $JAVA
  fi
}


appendLine()
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

    # is JAVA env var set?
    if [ ! -z "$JAVA" ]; then
        if [ -x "$JAVA" ]; then
            java_cmds=`appendLine "$java_cmds" "$JAVA"`
        fi
    fi;

    # is java in path
    local which_java=`quietWhich java`
    if [ ! -z $which_java ]; then
        if [ -x "$which_java" ]; then
            java_cmds=`appendLine "$java_cmds" "$which_java"`
        fi
    fi

    # is JAVA_HOME set?
    if [ ! -z "$JAVA_HOME" ]; then
        if [ -x "$JAVA_HOME/bin/java" ]; then
            java_cmds=`appendLine "$java_cmds" "$JAVA_HOME/bin/java"`
        fi
    fi;

    # is java in /usr/bin?
    if [ -x "/usr/bin/java" ]; then
        java_cmds=`appendLine "$java_cmds" "/usr/bin/java"`
    fi

    # are running on mac osx?
    if isOSX; then
        local osx_java_home=""
        if [ -x '/usr/libexec/java_home' ]; then
            osx_java_home=`/usr/libexec/java_home`
        fi
        if [ ! -z $osx_java_home ]; then
            if [ -x "$osx_java_home/bin" ]; then
                java_cmds=`appendLine "$java_cmds" "$osx_java_home/bin"`
            fi
        fi
    fi

    # search all known java home locations for java binaries
    # openjdk is usually in /usr/lib/jvm
    # sun jdk on centos/redhat in /usr/java
    
    java_home_parents_line="/usr/lib/jvm:/usr/java:/Library/Internet Plug-Ins:/System/Library/Frameworks/JavaVM.framework/Versions:/Library/Java/JavaVirtualMachines:/System/Library/Java/JavaVirtualMachines"
    IFS=":"
    for java_home_parent in $java_home_parents_line; do
        #echo "searching java_home_parent: $java_home_parent"
	for maybe_java_home in $java_home_parent/*; do
            [ -d "$maybe_java_home" ] || continue   

            if [ -x "$maybe_java_home/bin/java" ]; then
                java_cmds=`appendLine "$java_cmds" "$maybe_java_home/bin/java"`
            elif [ -x "$maybe_java_home/jre/bin/java" ]; then
                java_cmds=`appendLine "$java_cmds" "$maybe_java_home/jre/bin/java"`
            fi

            # osx path
            if [ -x "$maybe_java_home/Contents/Home/bin/java" ]; then
                java_cmds=`appendLine "$java_cmds" "$maybe_java_home/Contents/Home/bin/java"`
            elif [ -x "$maybe_java_home/Contents/Home/jre/bin/java" ]; then
                java_cmds=`appendLine "$java_cmds" "$maybe_java_home/Contents/Home/jre/bin/java"`
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


# java_bin=`findMinJavaVersion 1.7 <java_cmd separated by colon>`
findMinJavaVersion()
{
    local min_version="$1"
    local java_cmds_line="$2"
    
    # "1.7" -> extract java version
    local target_java_version=`extractPrimaryJavaVersion "$min_version"`

    IFS=":"
    for java_cmd in $java_cmds_line; do
        #echo "getting version from: $java_cmd"
        java_version=`"$java_cmd" -version 2>&1 | grep "version" | awk '{print $3}' | tr -d \" | awk '{split($0, array, ".")} END{print array[2]}'`
        #echo "java_version: $java_cmd -> $java_version"
        if [ "$java_version" != "" ] && [ $java_version -ge $target_java_version ]; then
             #echo "boom -- works!"
             echo $java_cmd
             return 1
        fi
    done

    return 0
}



#
# Finds best java home to use -- either JAVA_HOME if set, otherwise it'll attempt
# to find a JAVA_HOME to use and sort them by version.
#
findJavaHome()
{
    # if the JAVA_HOME env var is set use it
    if [ ! -z "$JAVA_HOME" ]; then
        # resolve symbolic link, if needed ('-f' doesn't work in OS X...)
        [ -L $JAVA_HOME ] && echo `readlink -f $JAVA_HOME`
        [ -L $JAVA_HOME ] || echo $JAVA_HOME
    # try to the find the latest JAVA_HOME by looking in certain locations
    else
        # temp file based on this shell's process id
        TMPJ=/tmp/j$$

        # If a java runtime is not defined, search the following
        # directories for a JVM and sort by version. Use the highest
        # version number.
        # Java search path
        JAVA_LOCATIONS="\
            /usr/java \
            /usr/bin \
            /usr/local/bin \
            /usr/local/java \
            /usr/local/jdk \
            /usr/local/jre \
            /usr/lib/jvm \
            /opt/java \
            /opt/jdk \
            /opt/jre \
        "
        JAVA_NAMES="java jdk jre"
        for N in $JAVA_NAMES ; do
            for L in $JAVA_LOCATIONS ; do
                [ -d $L ] || continue
                find $L -name "$N" ! -type d | grep -v threads | while read J ; do
                    [ -x $J ] || continue
                    VERSION=`eval $J -version 2>&1`
                    [ $? = 0 ] || continue
                    VERSION=`expr "$VERSION" : '.*version.*"\([0-9._]*\)"'`
                    [ "$VERSION" = "" ] && continue
                    expr $VERSION \< 1.2 >/dev/null && continue
                    # while [ -L $J ] ; do
                    #    J=`readlink $J`
                    # done
                    echo $VERSION:$J
                done
            done
        done | sort | tail -1 > $TMPJ
        TMPJAVA=`cat $TMPJ | cut -d: -f2`
        TMPJVERSION=`cat $TMPJ | cut -d: -f1`

        TMPJAVA_HOME=`dirname $TMPJAVA`
#        while [ ! -z "$TMPJAVA_HOME" -a "$TMPJAVA_HOME" != "/" -a \
#            ! -f "$TMPJAVA_HOME/lib/tools.jar" -a ! -f "$TMPJAVA_HOME/Classes/classes.jar" ] ; do
        while [ ! -z "$TMPJAVA_HOME" -a "$TMPJAVA_HOME" != "/" -a ! -f "$TMPJAVA_HOME/lib/tools.jar" ] ; do
            TMPJAVA_HOME=`dirname $TMPJAVA_HOME`
        done
        [ "$TMPJAVA_HOME" = "" ] && TMPJAVA_HOME=

        # remove the temporary file
        rm -f $TMPJ

        echo $TMPJAVA_HOME
    fi
}

# JAVA_CMD=`findJavaHomeJavaCommand $JAVA_HOME`
findJavaHomeJavaCommand()
{
  TMPJAVA_HOME=$1
  # search for $JAVA_HOME/jre/bin/java first
  if [ -x $TMPJAVA_HOME/jre/bin/java ]
  then
    echo $TMPJAVA_HOME/jre/bin/java
  else
    if [ -x $TMPJAVA_HOME/bin/java ]
    then
      echo $TMPJAVA_HOME/bin/java
    else
      echo "Cannot find a valid JRE or JDK command in JAVA_HOME=$JAVA_HOME. Please correct and re-run" 2>&2
      exit 1
    fi
  fi
}

appendJavaClasspath()
{
  if [ -z $1 ]; then
    echo $2
  else
    echo "$1:$2"
  fi
}

# JAVA_CLASSPATH=`buildJavaClasspath dirToSearch`
buildJavaClasspath()
{
  # path to main application directory
  TMPAPPDIR=$1

  if [ -d $TMPAPPDIR ]; then
    for file in $TMPAPPDIR/*.jar; do
      TMPCLASSPATH=`appendJavaClasspath "$TMPCLASSPATH" "$file"`
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
  TIMEOUT=60
  while running $TMPPID && [ $TIMEOUT -gt 0 ]; do
    kill $PID 2>/dev/null
    sleep 1
    echo -n "."
    let TIMEOUT=$TIMEOUT-1
  done
  if [ ! $TIMEOUT -gt 0 ]
  then 
      echo "Unable to kill app within timeout; may need to kill it manually [pid: $TMPPID]"
  else
      rm -f $TMPPID
  fi
}
