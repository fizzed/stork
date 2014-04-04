<service>
  <id>${config.name}</id>
  <name>${config.displayName}</name>
  <description>${config.shortDescription}</description>
  <executable>java</executable>
  <arguments>-cp %BASE%\..\lib\* ${config.javaArgs} ${config.mainClass} ${config.appArgs}</arguments>
  <logmode>rotate</logmode>
  <logpath>%BASE%\..\${config.logDir}</logpath>
  <log mode="reset"/>
  <workingdirectory>%BASE%\..</workingdirectory>
</service>