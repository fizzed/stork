package com.fizzed.stork.maven;

import com.fizzed.stork.launcher.Merger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin akin to the "stork-merge" command-line app but
 * directly accesses Java library (negating local install requirements).
 * 
 * @author joelauer
 */
@Mojo(name = "merge",
      defaultPhase = LifecyclePhase.PACKAGE,
      threadSafe = true
    )
public class MergeMojo extends AbstractMojo {
    
    /**
     * One or more input directories/files to merge (order is important).
     * 
     * @since 1.2.0
     */
    @Parameter(property = "inputFiles", required = true)
    protected String[] inputFiles;
    
    /**
     * Directory to output generated files.
     *
     * @since 1.2.0
     */
    @Parameter(property = "outputFile", required = true)
    protected File outputFile;
    
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.outputFile == null || this.outputFile.equals("")) {
            getLog().info("Skipping (outputFile is empty)");
            return;
        }
    
        List<File> files = new ArrayList<>();
        for (String f : inputFiles) {
            files.add(new File(f));
        }
        
        try {
            Merger.merge(files, outputFile);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
}
