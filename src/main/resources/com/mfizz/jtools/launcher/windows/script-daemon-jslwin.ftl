
[defines]

;This section defines variables, which can be used elsewhere in the ini file
;A variable refenrencing itself e.g. "PATH=%PATH%;c:\java\test" will result in an import from the environment. 
;JSL will first look in the normal environment then in the system environment of the registry as a fallback.
;Any variable not in the defines section will result in a environment lookup as well.
;Variable substitution is possible in any value in the ini file
;% is escaped by %%
;PATH= %PATH%
;JAVA = %JAVA_HOME%
;P1 = c:\java\test\jsl_0_9_9o
;P2 = %P1%

;Comma seperated list of variables to be exported to the environment
;Supersedes the "path" parameter further down. 
;Do not use the path parameter if you also set "PATH"in the export parameter as precedence is undefined.
;export = CLASSPATH,PATH

[service]
appname = ${config.name}
servicename = ${config.displayName}
displayname = ${config.displayName}
servicedescription = ${config.shortDescription}

;Size of internal buffer for string handling
;increase if you use very long command line parameters e.g. a very long classpath
stringbuffer = 16000

;service dependencies can be added
;as a comma separated string "dep1,dep2"
;dependencies=depend2

;service start type
;auto demand disabled	
;default is auto
starttype=auto

;Allow interaction with desktop
;This is a service configuration flag; default is false
;interactwithdesktop = false

;load ordering group
;loadordergroup=someorder	

;account under which service runs
;default is system
;account=mroescht-PC\mroescht

;password for above account
;system need not provide a password
;obfuscation of password is not supported. It is actually not needed.
;The password specified here is ONLY used during the jsl -install command. It can (and should) be deleted after installation.
;password=somepwd

;Allocate a console and register a console event handler to catch shutdown events.
;Default is true; options are FALSE TRUE false true YES NO yes no
;This option has two effects:
;1. If active it catches the logoff events and prevents the service from erroneously stopping in this case.
;2. It creates a console window if interaction with the desktop is allowd.
;Effect 1 can be achieved in Java 1.3.1 and higher by using the -Xrs option.
;Effect 2 may or may not be desired.
useconsolehandler=false

;Call <stopclass>.<stopmethod> through JNI so stop the JVM.
;This can be used an alternative to the ServiceStopper 
;When all three parameters are defined the JSL will try to use JNI to stop the service. 
;If not it will use a tcp/ip connection to the stopport.
;The simplest way to use this functionality is to use the Systen.exit() call as specified below. 
;Nevertheless you can call any static method in your code.   
;The method called is expected to terminate the JVM one way or the other. It can directly 
;call System.exit() or make another Thread do it make the main method return. 
;The method can return imediately if desired. If the JVM doesn't stop another attempt can be made to 
;stop the service at a later time.
stopclass=java/lang/System 
stopmethod=exit 

;Take care to specify the right signature for your method. void System.exit( int code ) has 
;the sifnature specified below. void myMethod() has the signature ()V. Please refer to the
;JNI documentation for details.
stopsignature=(I)V

;Parameters can be passed in "stopparam". 
;Note that parameter passing is only implemented for the special case that the signature of the 
;method is (String[]). Generic parameter passing is cumbersome to implement in C so I choose
;to implement only the most common case.
;stopsignature=([Ljava/lang/String;)V
;The parameter list is parsed into an array. Delimiters are spaces or quoted strings.
;stopparams=STOP "OR NOT STOP"

;Name and signature of service pause method. Will be called on a SERVICE_CTRL_PAUSE event.
;Please note that unless you have configured all pause and continue arguments 
;you will not see the pause button enbaled in the service control manager GUI
;pauseclass=com.roeschter.jsl.TelnetEcho
;pausemethod=pause 
;pausesignature=()V
;pauseparams=

;Name and signature of service continue method. Will be called on a SERVICE_CTRL_CONTINUE event.
;Please note that unless you have configured all pause and continue arguments 
;you will not see the pause button enbaled in the service control manager GUI
;contclass=com.roeschter.jsl.TelnetEcho 
;contmethod=cont 
;contsignature=()V
;contparams=

;Value of the PATH environement variable being set by the service. 
;PATH will remain unchanged if this is empty.
;path=c:\util

;Redirect C level stdout and stderr to the specified files
;Please note that those are diffrent files then the ones used below for 
;Java level stdout and stderr
;Default is no redirection
;stdout=c:\stdout.log
;stdoutappend=no
;stderr=c:\stderr.log
;stderrappend=no

;Redirect JAVA level System.out and System.err to the specified files
;This simply sets new output streams for System.out and System.err after
;the JVM is initialized but before the main method is called.
;You might find that this option has no effect at all for some applications, as the 
;JAVA application is of course free to redirect System.out and System.err 
;to some other log mechanism.
;Default is no redirection
;Default is to overwrite file
;systemout=c:\systemout.log
;systemoutappend=no
;systemerr=c:\systemerr.log
;systemerrappend=no

;Explicitely call this method and wait for it to return before setting the service to status running
;This method will be called before the main method
;confirmrunclass=com.roeschter.jsl.TelnetEcho 
;confirmrunmethod=confirmRunning 
;confirmrunsignature=()V
;confirmrunparams=

;This method will be called before the main method of the start class specified on command line 
;but after the JVM is fully initialized and stdout and stderr redirection have been performed.
;This method must return! It is called from the same thread as the main method.
;In order for the normal JVM start behaviour to continue this method must return 0.
;It may legally return any other value or throw any exception, which in both cases will result in
;a regular termination of the JVM much in the same way as if an uncatched exception had been thrown
;in the main method.
;It may legally start other threads in the JVM, which will behave in the same way as started from
;the main method.
;In fact you will be able to produce pretty much the same behaviour as calling the static method first in
;your main method.
;The example method present here is a scheduler which will in turn run the code specified 
;in its modules list. Some utilities are provided.

;Uncomment the premainclass to enable
;premainclass=com/roeschter/jsl/PreMainScheduler
premainmethod=run 
premainsignature=()I
premain.modules=threaddump

premain.threaddump.class=com.roeschter.jsl.ThreadDumpListener
premain.threaddump.method=start
premain.threaddump.wait=3000
premain.threaddump.critical=no
premain.threaddump.interface=127.0.0.1

;Report service stopped on exit of main thread
;you might want to set this to yes if your service terminates "silently"
;It is recommended that a service is stopped by calling System.exit() at some time, 
;either externally or internally. This provides a nice and clean shutdown hook.
;If the service terminates silently by just ending the last thread this might result 
;in the service manager not recognizing the fact and denying restart. Use yes in this case.
;It should usually be safe to use reportservicestoppedonmainthreadexit=true
;even if you use other threads then main. The shutdown code will wait for non daemon threads to stop
;I can't remember why I made this parameter optional. It must have been useful for some people or in some situations
;Default is no
;reportservicestoppedonmainthreadexit = no

;Behaviour in case of JVM exiting with an error 
;Define whether an execption in the main method should be considered an error
;Use this exit code in case of an exception.
;exceptionerrorcode=0

;Desired behaviour for a non zero exit code (including exceptions as specified above)
;Options:
;ignore 		terminate without error (default)
;returncode 		exit with error code reported by the JVM to to the service manager
;fatal			don't report regular service stop to service manager making it believe 
;a fatal error had occured in the service (this is sometimes desirable if fatal error 
;recovery mechanisms are in place)
;onexiterror=fatal

;Use this executable for registering the service.
;Default is the executable used for installation
;modulepath="e:\java\test\jsl_0_9_9e\release\jsl.exe -ini e:\java\test\jsl_0_9_9e\release\jsl2.ini"


[java]
;Path to the java runtime used
;If this option is not used the default from the registry will be used
;jrepath=..\..\..\jdk16
;jrepath=c:\java\jdk18

;Type of jvm to be used. Alternative mechanism to specifiying -client or -server on the command line.
;Can use any JVM name, which is diffrent from the command line which will only allow -client and -server
;Useful when using a JVM diffrent from Suns implementation.
;Jvmtype can be a comma seperated list. JSL will first find a JVM installation location. THEN it will load the first 
;type of jvm in the list it can find. It will NOT search in alternative JVM locations for alternative jvm types. 
;Be careful what JVM is actullay installed and used before you report a bug on this feature.
;jvmtype=server
jvmtype=server,client,hotspot,classic

;working directory
;If the working directory path starts with a "." it will be replaced with the .ini directory path
;This is neccessary because a service does not inherit a working directory from the shell. Therefore "." would be meaningless.
;This logic for "." is only required for the wrkdir bas all other paths will resolve a "." against the working dir once it is set.
;wrkdir=c:\java\test\jsl_0_9_9m\release

; since we use a std layout of <app home>\bin then this will be app home
wrkdir=.\..

;The java command line
;The entry method below using a paramter list still works but the command line variant is more convenient, 
;cmdline = -cp %P1%\src com.roeschter.jsl.TelnetEcho
;cmdline = -cp ..\src com.roeschter.jsl.TelnetEcho

cmdline = -cp lib\* ${config.javaArgs} ${config.mainClass} ${config.appArgs}