package com.fizzed.stork.gradle.tasks

import com.fizzed.stork.launcher.Configuration
import com.fizzed.stork.launcher.PlatformConfiguration

class LauncherExtension {
    
    Configuration.Platform[] platforms
    String name
    String displayName
    String domain
    String shortDescription
    String longDescription
    String mainClass
    Configuration.Type type
    Configuration.WorkingDirMode workingDirMode
    String appArgs = ""
    String javaArgs = ""
    String extraAppArgs = ""
    String extraJavaArgs = ""
    String minJavaVersion = "1.6"
    Integer minJavaMemory = null
    Integer maxJavaMemory = null
    Integer minJavaMemoryPct = null
    Integer maxJavaMemoryPct = null
    boolean includeJavaXrs = true
    boolean symlinkJava = false
    boolean includeJavaDetectHelper = false
    Integer daemonMinLifetime = 5

    private Map<String,PlatformConfiguration> platformConfigurations

    Configuration toConfiguration() {
        Configuration cfg = new Configuration()
        cfg.file = new File(name + ".yml")
        cfg.name = name
        cfg.displayName = displayName
        cfg.Domain = domain
        cfg.ShortDescription = shortDescription
        cfg.LongDescription = longDescription
        cfg.MainClass = mainClass
        cfg.Type = type
        cfg.WorkingDirMode = workingDirMode
        cfg.AppArgs = appArgs
        cfg.JavaArgs = javaArgs
        cfg.ExtraAppArgs = extraAppArgs
        cfg.ExtraJavaArgs = extraJavaArgs
        cfg.MinJavaVersion = minJavaVersion
        cfg.MinJavaMemory = minJavaMemory
        cfg.MaxJavaMemory = maxJavaMemory
        cfg.MinJavaMemoryPct = minJavaMemoryPct
        cfg.MaxJavaMemoryPct = maxJavaMemoryPct
        cfg.IncludeJavaXrs = includeJavaXrs
        cfg.SymlinkJava = symlinkJava
        cfg.IncludeJavaDetectHelper = includeJavaDetectHelper
        cfg.DaemonMinLifetime = daemonMinLifetime

        Set<Configuration.Platform> ps = new HashSet<>()
        ps.addAll(platforms)
        cfg.platforms = ps

        if (platformConfigurations != null) {
            Map<Configuration.Platform, PlatformConfiguration> pcm = new HashMap<>()
            for (Map.Entry<String, PlatformConfiguration> pc : platformConfigurations.entrySet()) {
                pcm.put(Configuration.Platform.valueOf(pc.key), pc.value)
            }
        }

        return cfg
    }

}
