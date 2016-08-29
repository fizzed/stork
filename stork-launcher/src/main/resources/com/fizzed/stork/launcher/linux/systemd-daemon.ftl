[Unit]
Description=${config.shortDescription}
After=network.target

[Service]
EnvironmentFile=/etc/default/${config.name}
ExecStart=${config.getPlatformPrefixDir("LINUX")}/${config.name}/bin/${config.name} --start
PIDFile=${config.getPlatformPrefixDir("LINUX")}/${config.name}/run/${config.name}.pid
KillMode=process
Restart=on-failure
User=${config.getPlatformUser("LINUX")!""}
Group=${config.getPlatformGroup("LINUX")!""}

[Install]
WantedBy=multi-user.target
Alias=${config.name}.service