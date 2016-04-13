Stork by Fizzed
=======================================

## Canonical java application layout

All of Stork's tools need to know where to look for various files in order to
function in a standard way.  Stork uses the following conventional app layout:

    <app_name>/	(
        bin/	(launcher scripts, overwrite on upgrade)
        lib/	(all jars, overwrite on upgrade, fat jars not recommended)
        share/  (arch indep data for install/running/info; overwrite on upgrade)
        conf/	(config files; retain on upgrade)
        data/   (not included in assembly/install; retain on upgrade)
        log/    (not included in assembly/install; retain on upgrade)
        run/    (not included in assembly/install; retain on upgrade)

### bin/ (executables)

For all read-only executables.  These are the binaries the user will execute to
run your console application or start/stop your daemon.  The executables should
look and feel like a native application.

Examples include batch files or shell scripts to start your Java app. Assume
file permissions of 0755.

### lib/ (libraries)

All shared files and libraries required for running the application(s).

Examples include jar files containing compiled Java classes. Assume file permissions
of 0644.

### conf/ (configuration data)

All configuration files for the application(s). Any files in this directory
need to be carefully examined during an upgrade -- since the user may have
edited the config for their specific system.  Assume file permissions of 0644.

### share/ (architecture-independent data)

For all read-only architecture independent data files.

Examples would include sql scripts to setup databases; linux/unix init.d scripts, or
documentation. Assume this data will be overwritten on application upgrades.
Assume file permissions of 0644.

### data/ (variable state information)

State information is data that programs modify while they run, and that pertains
to one specific host.  State information remains valid after a reboot, should
not be logging output, and should not be spooled data.

Files in this directory should be retained between upgrades.

Examples would include an application's database.  On Linux/UNIX, this could/may be
symlinked to /var/data/<app_name>.  Assume file permissions of 0644.

### log/ (logfiles)

Logfiles for application (startup, runtime, etc.). It must be acceptable to
truncate or delete files in this directory w/o affecting the application on
its next invocation.

Files in this directory may be retained between upgrades, but assume they will
be deleted.  On Linux/UNIX, this could be symlinked to /var/log/<app_name>.

### run/ (run-time variable data)

This directory contains system information data describing the system since it
was booted. Files under this directory may/will be cleared (removed or truncated
as appropriate) at the beginning of the boot process. On some versions of linux,
/var/run is mounted as a temporary file system.

Examples would include an application's process id (pid) file or named sockets.
On Linux/UNIX, this could be symlinked to /var/run/<app_name>.
