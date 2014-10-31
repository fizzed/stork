from fabric.api import *
import os
import sys
sys.dont_write_bytecode = True

work_dir = "work"

def deploy(assembly):
	# default as though user passed us a directory
	assembly_dir = assembly	

	# .tar.gz or directory?
	if os.path.isfile(assembly):
		# explode to work directory
		local("rm -Rf %s" % work_dir)
		local("mkdir -p %s" % work_dir)
		local("tar zxf '" + assembly + "' -C %s" % work_dir)
		# find directory within work
		files = os.listdir(work_dir)
		assembly_dir = work_dir + "/" + files[0]

	print "Assembly dir: " + assembly_dir

	# last part of directory path is the name of the artifact
	assembly_name = os.path.basename(assembly_dir)
	print "Assembly name: " + assembly_name

	# name should end with the version of the artifact...
	

	print "Deploying " + assembly
	# upload the source tarball to the temporary folder on the server
   	put(assembly, '/tmp/')
	#run('uptime')
	#run('java -version')
