#!/bin/bash
# /etc/init.d/${config.name}
# Debian-compatible startup script
# Via Mfizz Jtools Launcher (http://mfizz.com)
#
### BEGIN INIT INFO
# Provides:          ${config.name}
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog $network
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: ${config.shortDescription}
# Description:       ${config.longDescription}
### END INIT INFO

PATH=/bin:/usr/bin:/sbin:/usr/sbin

NAME="${config.name}"
SCRIPTNAME="/etc/init.d/${config.name}"
APP_HOME="/usr/local/${config.name}"

# any system defaults?
[ -r /etc/default/$NAME ] && . /etc/default/$NAME

case "$1" in
  start)
    "$APP_HOME/bin/$NAME" -start
    ;;
  stop)
    "$APP_HOME/bin/$NAME" -stop
    ;;
  restart)
    "$APP_HOME/bin/$NAME" -stop
    "$APP_HOME/bin/$NAME" -start
    ;;
  status)
    "$APP_HOME/bin/$NAME" -status
    ;;
  *)
    echo "Usage: $SCRIPTNAME {start|stop|status|restart}" >&2
    exit 3
    ;;
esac

exit 0