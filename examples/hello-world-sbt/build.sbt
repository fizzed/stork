name := "hello-world"

version := "1.0"

scalaVersion := "2.11.1"

// bind stork to generate launchers during compile
//compile in Compile <<= (compile in Compile).dependsOn(storkLauncherGenerate)
