import deploy

class CleanAndDeployTask(deploy.DeployTask):
    def pre_deploy(self):
	print "cleaning before deploy..."
        #print yellow('clean up /var/lib/tomcat7/work/')
        #sudo('rm -fr /var/lib/tomcat7/work/*')

deploy = deploy.DeployTask()
#deploy = CleanAndDeployTask()
