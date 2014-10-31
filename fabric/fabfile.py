from fabric.api import *
import os, sys, time, datetime, fabric
sys.dont_write_bytecode = True

class Deployer:
    # dir for work required 
    work_dir = "work"
    now_str=datetime.datetime.fromtimestamp(time.time()).strftime('%Y%m%d%H%M%S')

    # /path/to/assembly-1.0.0.tar.gz
    assembly_tgz_file = None
    # assembly-1.0.0.tar.gz (no parent path)
    assembly_tgz_name = None
    # /path/to/assembly-1.0.0
    assembly_dir = None
    # assembly-1.0.0 (no parent path)
    assembly_name = None 

    # app (extracted from assembly/descriptor)
    app_name = None
    app_version = None
    app_snapshot = False
    
    # daemons in app (determined from share/init.d/*.init)
    app_daemon_names = []
    
    app_user = "daemon"
    app_group = "daemon"

    # dir prefix that app will go into /opt/app_name
    remote_prefix_dir = "/opt"
    # app dir (prefix/app)
    remote_app_dir = None
    # /opt/app_name/current
    remote_current_dir = None
    # /opt/app_name/version-X.X.X
    remote_version_dir = None
    # where current points to original
    remote_current_version_dir = None
    # number of previous versions to retain (after success)
    retain_versions = 1

def make_clean_work_dir(d):
    local("rm -Rf %s" % d.work_dir)
    local("mkdir -p %s" % d.work_dir)

def set_app_info_from_assembly_name(d, assembly_name):
    puts("Reading app info from assembly name: " + assembly_name, show_prefix=False)	

    app_name = assembly_name

    # strip -SNAPSHOT
    if app_name.endswith("-SNAPSHOT"):
        d.app_snapshot = True
        app_name = app_name.replace("-SNAPSHOT", "")

    # find version then strip it out
    version_index = app_name.rindex("-")
    d.app_version = app_name[version_index+1:len(app_name)]
        
    # app name is what remains
    d.app_name = app_name[0:version_index]

    puts("App name: " + d.app_name, show_prefix=False)
    puts("App version: " + d.app_version, show_prefix=False)
    puts("App snapshot? " + str(d.app_snapshot), show_prefix=False)
    
def set_app_daemons_from_assembly_dir(d):
    puts("Reading " + d.assembly_dir + "/share/init.d/*.init to detect daemons...", show_prefix=False)

    initd_dir = d.assembly_dir + "/share/init.d"
    init_files = os.listdir(initd_dir)
    
    for init_file in init_files:
        if init_file.endswith(".init"):
            if d.app_daemon_names is None:
                d.app_daemon_names = []
            app_daemon_name = init_file[0:len(init_file)-5]
            d.app_daemon_names.append(app_daemon_name)
            puts("Detected app daemon: " + app_daemon_name, show_prefix=False)

def unpack_or_pack(d, assembly):
    if os.path.isfile(assembly):
        # verify we can support unpacking it
        if not assembly.endswith(".tar.gz"):
            fabric.utils.error("Only assemblies of .tar.gz ext are supported")

        # create clean work dir to unpack to
        make_clean_work_dir(d)

        # unpack to fresh work directory
        local("tar zxf '" + assembly + "' -C %s" % d.work_dir)

        # unpacked file should have only created a single dir
        unpacked_files = os.listdir(d.work_dir)
        if len(unpacked_files) != 1:
            fabric.utils.error("Assembly tarballs expected to contain only a single parent dir")	

        d.assembly_name = unpacked_files[0]
        d.assembly_dir = d.work_dir + '/' + d.assembly_name
        d.assembly_tgz_file = assembly
        d.assembly_tgz_name = os.path.basename(d.assembly_tgz_file)
    else:
        # while we could tarball the dir up to support this, for now just error
        fabric.utils.error("Only assemblies of .tar.gz are supported (unpacked form not supported")

    puts("Assembly tgz: " + d.assembly_tgz_file, show_prefix=False)
    puts("Assembly unpacked: " + d.assembly_dir, show_prefix=False)

def remove_temp_remote_assembly(d):
    with cd('/tmp'):
        run('rm -Rf "' + d.assembly_tgz_name + '" "' + d.assembly_name + '"')

def run_rsync(local_dir, remote_dir, delete=False, excludes=[]):
    excludes = "".join(map(lambda e: " --exclude="+e, excludes))
    deletes = ""
    if delete:
        deletes = " --delete "
    rsync_cmd = "rsync -avrtc" + excludes + deletes + " --force -e \"ssh -oStrictHostKeyChecking=no -p " + env.port + "\" " + local_dir + " " + env.user+"@"+env.host+":"+remote_dir
    
    if env.password is None or env.password == "":
        # run rsync without expect
        local(rsync_cmd)
    else:
        # run rsync with expect to pass it the password
        local("expect -c 'exp_internal 0; set timeout 20; spawn " + rsync_cmd + "; expect \"*?assword:*\"; send \""+env.password+"\\n\"; expect eof'")

def start_daemon(d, daemon_name):
    initd_file = "/etc/init.d/" + daemon_name
    if fabric.contrib.files.exists(initd_file, use_sudo=True) or fabric.contrib.files.is_link(initd_file, use_sudo=True):
        puts("Starting init.d service: " + daemon_name)
        result = sudo(initd_file + ' start', shell=False)
        result.succeeded
    else:
        fabric.utils.error("Looks like init.d sevice " + initd_file + " does not yet exist")

def stop_daemon(d, daemon_name):
    initd_file = "/etc/init.d/" + daemon_name
    if fabric.contrib.files.exists(initd_file, use_sudo=True) or fabric.contrib.files.is_link(initd_file, use_sudo=True):
        puts("Stopping init.d service: " + daemon_name)
        result = sudo(initd_file + ' stop', shell=False)
        result.succeeded
    else:
        puts("Looks like init.d sevice " + initd_file + " does not yet exist")

def stop_daemons(d):
    for daemon_name in d.app_daemon_names:
        stop_daemon(d, daemon_name)

def write_daemon_defaults(d, daemon_name):
    for i in ["default","sysconfig"]:
        if fabric.contrib.files.exists('/etc/'+i):
            if not fabric.contrib.files.exists('/etc/'+i+'/'+daemon_name):
                sudo('echo "APP_HOME=\"'+d.remote_current_dir+'\"" > /etc/'+i+'/'+daemon_name)

def deploy(assembly):
    d = Deployer()

    unpack_or_pack(d, assembly)
    set_app_info_from_assembly_name(d, d.assembly_name)
    set_app_daemons_from_assembly_dir(d)

    # setup remote install targets
    d.remote_app_dir = d.remote_prefix_dir + "/" + d.app_name
    d.remote_current_dir = d.remote_app_dir + "/current"
    d.remote_version_dir = d.remote_prefix_dir + "/" + d.app_name + "/version-" + d.app_version
    if d.app_snapshot:
        d.remote_version_dir += "-" + d.now_str

    puts("App dir: " + d.remote_app_dir)
    puts("Current dir: " + d.remote_current_dir)
    puts("Version dir: " + d.remote_version_dir)

    # app version already installed?
    if fabric.contrib.files.exists(d.remote_version_dir, use_sudo=True):
        print "Remote version dir exists. App version already installed!"
        return

    # create version directory (where we will install to)
    sudo('mkdir -p %s' % d.remote_version_dir, shell=False)

    if not fabric.contrib.files.exists(d.remote_current_dir, use_sudo=True):
        puts("Current dir does not exist (fresh install detected/requested)")
        
        # TODO: none of the daemons should be installed yet...
        
        # fastest to copy over assembly tgz, unpack it, and install it
        remove_temp_remote_assembly(d)
        with cd('/tmp'):
            put(d.assembly_tgz_file, '/tmp')
            run('tar zxf "' + d.assembly_tgz_name + '"')
            
        # copy completed app over to final place
        with cd('/tmp/'+d.assembly_name):
            sudo('cp -R . "{}"'.format(d.remote_version_dir), shell=True)
            
    else:
        puts("Current dir exists (upgrade install detected)")        
        
        # what does "current" currently point to?
        d.remote_current_version_dir = run('readlink -f "%s"' % d.remote_current_dir)
        puts("Current dir currently -> " + d.remote_current_version_dir)
        
        # build new "assembly" by copying what's present and then rsync'ing what's different
        # this makes for a much, much faster deploy cycle when most jars aren't different
        remove_temp_remote_assembly(d)
        with cd('/tmp'):
            run('mkdir "{}"'.format(d.assembly_name))
        
        # conf, data, log, and run dirs are retained on upgrades (do not sync)
        run('rsync -at "{}" "{}" --exclude="conf" --exclude="data" --exclude="log" --exclude="run"'.format(d.remote_current_version_dir+'/', '/tmp/'+d.assembly_name+'/'))     
        
        # rsync everything over (except conf)
        run_rsync(local_dir=d.assembly_dir + '/', remote_dir='/tmp/'+d.assembly_name+'/', delete=True, excludes=["conf"])
        
        # copy completed app over to final place
        with cd('/tmp/'+d.assembly_name):
            sudo('cp -R . "{}"'.format(d.remote_version_dir), shell=True)
        
        stop_daemons(d)
        
        # move current conf, data, log, and run dirs
        with cd(d.remote_current_version_dir):
            sudo('for f in conf data log run; do if [ -d "$f" ]; then mv -n "$f" "{}"; echo "$f moved (exists in current version)"; else echo "$f skipped (does not exist in current version)"; fi; done'.format(d.remote_version_dir))
    
    # either fresh or upgrade installs -- ok to remove temp assembly
    remove_temp_remote_assembly(d)
    
    #create symlink from version dir to current dir
    with cd(d.remote_app_dir):
        sudo('rm -f "{}"'.format(os.path.basename(d.remote_current_dir)), shell=True)
        sudo('ln -s "{}" "{}"'.format(os.path.basename(d.remote_version_dir), os.path.basename(d.remote_current_dir)), shell=True)    
    
    # fix ownership
    sudo('chown -R {}.{} "{}"'.format(d.app_user, d.app_group, d.remote_version_dir), shell=True)
    # fix permissions
    sudo('chmod -R 755 "{}"'.format(d.remote_current_dir+'/bin'), shell=True)
    sudo('chmod -R 755 "{}"'.format(d.remote_current_dir+'/share/init.d'), shell=True)
    
    for daemon_name in d.app_daemon_names:
        # install init.d?
        initd_file = "/etc/init.d/" + daemon_name
        if not fabric.contrib.files.exists(initd_file, use_sudo=True) and not fabric.contrib.files.is_link(initd_file, use_sudo=True):
            # create symlink
            sudo('ln -s "{}" "{}"'.format(d.remote_current_dir+'/share/init.d/'+daemon_name+'.init', initd_file), shell=True)
        
        #install sysconfig script (as needed)
        write_daemon_defaults(d, daemon_name)
        
        # TODO: configure init script to start at boot???
        
        # start daemon...
        start_daemon(d, daemon_name)
    
    # remove N number of previous versions (to keep things tidy)?
    if d.retain_versions < 0:
        puts("Purging old versions disabled (retain_versions < 0)")
    else:
        output = run('ls -t "{}"'.format(d.remote_app_dir))
        all_remote_version_dirs = output.split()
        
        # build list of versions that need deleted
        remote_version_name = os.path.basename(d.remote_version_dir)
        remote_version_names_to_retain = []
        remote_version_names_to_del = []
        for rvn in all_remote_version_dirs:
            if not rvn == "current" and not rvn == remote_version_name:
                if len(remote_version_names_to_retain) < d.retain_versions:
                    remote_version_names_to_retain.append(rvn)
                    puts("Going to retain version dir: " + rvn)
                else:
                    remote_version_names_to_del.append(rvn)
                    puts("Going to rm version dir: " + rvn)
                    
        # remove all these directories
        purge_cmd = "rm -Rf" + "".join(map(lambda e: " '"+e+"'", remote_version_names_to_del))
        with cd(d.remote_app_dir):
            sudo(purge_cmd, shell=True)

    print "Deployed " + assembly + "!"