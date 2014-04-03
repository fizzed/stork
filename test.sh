java_home_parents=("/usr/lib/jvm"
        "/Library/Internet Plug-Ins"
        "/usr/java"
    )

#    IFS=""
    for jpg in ${java_home_parents[*]}; do
        echo "dude: ${jpg}"
    done
