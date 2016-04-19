
 - Launcher support for systemd
 - Deploy rewrites systemd files for actual deploy path
 - Support for not using 'sudo' to do a deploy?  Or at least only using
   sudo to restart daemons.
   - Admin creates user/group on target machine for app
   - Admin creates /opt/<app> and gives ownership to user/group with 770 perms
   - Admin adds user that needs to deploy to group

 - Capture sudo password prompt?
 - Would require watching outputstream for a specific string and then writing
   a value to the outputstream in.  Probably complicated.