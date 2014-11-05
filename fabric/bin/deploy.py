from fabric.api import *
from fabric.tasks import Task
import os, sys, time, datetime, fabric
sys.dont_write_bytecode = True

class DeployTask(Task):
    name = 'deploy'
 
    def run(self, *args, **kwargs):
        #execute('tomcat7_stop')
        self.pre_deploy()        
        #print yellow('do deploy')
        self.post_deploy()
        #execute('tomcat7_start')        
 
    def pre_deploy(self):
        print "default pre_deploy"
        pass
 
    def post_deploy(self):
        pass
