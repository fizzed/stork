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
# Description:       ${config.longDescription}
### END INIT INFO

PATH=/bin:/usr/bin:/sbin:/usr/sbin

NAME="${config.name}"
SCRIPTNAME="/etc/init.d/${config.name}"
APP_HOME="/usr/local/${config.name}"
APP_USER="${config.getDaemonUser("LINUX")!""}"
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

case "$1" in
  start)
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -start"
    ;;
  run)
    # running with su does not correctly kill subshells - must have sudo to run
    $SUDO -u $APP_USER "$APP_HOME/bin/$NAME" -run
    ;;
  stop)
    "$APP_HOME/bin/$NAME" -stop
    ;;
  restart)
    "$APP_HOME/bin/$NAME" -stop
    $SU $APP_USER -s /bin/sh -m -c "\"$APP_HOME/bin/$NAME\" -start"
    ;;
  status)
    "$APP_HOME/bin/$NAME" -status
    ;;
  *)
    echo "Usage: $SCRIPTNAME {start|stop|status|restart|run}" >&2
    exit 3
    ;;
esac

exit 0