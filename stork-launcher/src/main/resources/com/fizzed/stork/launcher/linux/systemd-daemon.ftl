[Unit]
Description=${config.shortDescription}
After=network.target

[Service]
EnvironmentFile=/etc/default/${config.name}
ExecStart=${config.getPlatformPrefixDir("LINUX")}/${config.name}/bin/${config.name} --exec
ExecStop=${config.getPlatformPrefixDir("LINUX")}/${config.name}/bin/${config.name} --stop
PIDFile=${config.getPlatformPrefixDir("LINUX")}/${config.name}/run/${config.name}.pid
KillMode=process
Restart=on-failure
User=${config.getPlatformUser("LINUX")!""}
Group=${config.getPlatformGroup("LINUX")!""}
${config.getSystemdServiceSection()!""}

[Install]
WantedBy=multi-user.target