
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
    #[ -f $1 ] || return 1
    #PID=$(cat $1)
    #ps -p $PID >/dev/null 2>/dev/null || return 1
    #return 0
  local PID=$(cat "$1" 2>/dev/null) || return 1
  kill -0 "$PID" 2>/dev/null
}

isOSX()
{
  if [ platform == "osx" ]; then
    return 0
  else
    return 1
  fi
}

isLinux()
{
  if [ platform == "Linux" ]; then
    return 0
  else
    return 1
  fi
}

platform()
{
  UNAME=$(uname)
  if [ "$UNAME" == "Linux" ]; then
    echo "linux"
  elif [ "$UNAME" == "Darwin" ]; then
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
  elif isOSX; then
    #echo "on mac..."
    TMPMEM=`top -l 1 | awk '/PhysMem:/ {print $10}'`
    # strip off last M
    TMPMEMMB=${TMPMEM%%M}
    echo $TMPMEMMB
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
  if [ -z $1 ]; then
    echo $2
  else
    echo "$1\\n$2"
  fi
}


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

    # are running on mac osx?
    if isOSX; then
        local osx_java_home=""
        if [ -x '/usr/libexec/java_home' ]; then
            osx_java_home=`/usr/libexec/java_home`
        elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
            osx_java_home=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home
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
    local java_install_locations="\
            /usr/lib/jvm/* \
            /usr/java/* \
            /Library/Java/JavaVirtualMachines \
            /System/Library/Java/JavaVirtualMachines
        "

    for java_install_location in $java_install_locations; do
        [ -d $java_install_location ] || continue   

        if [ -x "$java_install_location/bin/java" ]; then
            java_cmds=`appendLine "$java_cmds" "$java_install_location/bin/java"`
        fi

        # osx path
        if [ -x "$java_install_location/Contents/Home/bin/java" ]; then
            java_cmds=`appendLine "$java_cmds" "$java_install_location/bin/java"`
        fi
    done

    echo -e "$java_cmds"
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
      # ignore anything ending in -sources.jar
      if [[ ! "$file" == *-sources.jar ]]; then
        TMPCLASSPATH=`appendJavaClasspath "$TMPCLASSPATH" "$file"`
        #if [ ! -z $TMPCLASSPATH ]; then TMPCLASSPATH="$TMPCLASSPATH:"; fi
        #TMPCLASSPATH=$TMPCLASSPATH$file
      fi
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
  while running $TMPPID && [ $TIMEOUT -gt 0 ]
  do
    kill $PID 2>/dev/null
    sleep 1
    let TIMEOUT=$TIMEOUT-1
  done
  if [ ! $TIMEOUT -gt 0 ]
  then 
      echo "Unable to kill app within timeout; may need to kill it manually [pid: $TMPPID]"
  else
      rm -f $TMPPID
  fi
}
