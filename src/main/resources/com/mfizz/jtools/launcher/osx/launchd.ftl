<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
    <dict>
        <!--
        <key>StandardOutPath</key>
        <string>/var/log/jenkins/jenkins.log</string>
        <key>StandardErrorPath</key>
        <string>/var/log/jenkins/jenkins.log</string>
        -->
        <!--
        <key>UserName</key>
	<string>daemon</string>
	<key>GroupName</key>
	<string>daemon</string>
        -->
	<key>KeepAlive</key>
	<true/>
        <key>RunAtLoad</key>
	<true/>
        <!-- Contains a unique string that identifies your daemon to launchd -->
	<key>Label</key>
	<string>${config.domain}.${config.name}</string>
	<key>ProgramArguments</key>
	<array>
            <string>/Library/Application Support/Jenkins/jenkins-runner.sh</string>
	</array>
	
    </dict>
</plist>