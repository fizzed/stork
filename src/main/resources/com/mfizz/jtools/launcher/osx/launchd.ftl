<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
    <dict>
        <key>Label</key><string>${config.domain}.${config.name}</string>
        <key>KeepAlive</key><true/>
        <key>RunAtLoad</key><true/>
        <key>WorkingDirectory</key><string>/Users/Shared/${config.displayName}</string>
        <key>Disabled</key><false/>
        <key>LaunchOnlyOnce</key><true/>
        <!-- application directory must be owned by the user below -->
        <!--<key>UserName</key><string>daemon</string>-->
	<!--<key>GroupName</key><string>daemon</string>-->
        <key>EnvironmentVariables</key>
        <dict>
            <key>LAUNCHER_DEBUG</key>
            <string>1</string>
            <key>SKIP_PID_CHECK</key>
            <string>1</string>
        </dict>
        <!-- relative to working directory above -->
        <key>StandardOutPath</key><string>log/${config.name}.stdout</string>
        <key>StandardErrorPath</key><string>log/${config.name}.stderr</string>
        <key>ProgramArguments</key>
        <array>
            <string>bin/${config.name}</string>
            <string>-run</string>
        </array>
    </dict>
</plist>