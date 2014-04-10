#!/bin/bash

#
# /etc/init.d/${config.name}
# Redhat and/or debian-compatible startup script
# Generated via Mfizz Jtools Launcher (http://mfizz.com)
#

### BEGIN INIT INFO
# Provides:          ${config.name}
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: ${config.shortDescription}
# Description:       ${config.longDescription!""}
### END INIT INFO

PATH=/bin:/usr/bin:/sbin:/usr/sbin

NAME="${config.name}"
SCRIPTNAME="/etc/init.d/${config.name}"
APP_HOME="/opt/${config.name}"
APP_USER="${config.getDaemonUser("LINUX")!""}"
APP_GROUP="${config.getDaemonUser("LINUX")!""}"
RUN_DIR="/var/run/${config.name}"
LOG_DIR="/var/log/${config.name}"
SU="/bin/su"
SUDO="sudo"

# make sure we are run as root
if [ `id -u` -ne 0 ]; then
    echo "The $NAME init script can only be run as root"
    exit 1
fi

# any system defaults (sysconfig on redhat; default on debian)
[ -r /etc/sysconfig/$NAME ] && . /etc/sysconfig/$NAME
[ -r /etc/default/$NAME ] && . /etc/default/$NAME

# run/log directories may have been removed from prior invocation
if [ ! -d "$RUN_DIR" ]; then
    mkdir -p "$RUN_DIR"
    chown -R $APP_USER:$APP_GROUP "$RUN_DIR"
fi
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    chown -R $APP_USER:$APP_GROUP "$LOG_DIR"
fi

# everything needs to be run as requested user
case "$1" in
  start)
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -start"
    ;;
  run)
    # running with su does not correctly kill subshells - must use sudo to run
    $SUDO -u $APP_USER "$APP_HOME/bin/$NAME" -run
    ;;
  stop)
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -stop"
    ;;
  restart)
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -stop"
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -start"
    ;;
  status)
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -status"
    ;;
  *)
    echo "Usage: $SCRIPTNAME {start|stop|status|restart|run}" >&2
    exit 3
    ;;
esac

exit 0