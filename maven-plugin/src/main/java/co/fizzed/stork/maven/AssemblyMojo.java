package co.fizzed.stork.maven;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Stages and assemble a maven project into a stork assembly tarball.
 * 
 * @author joelauer
 */
@Mojo(name = "assembly",
      defaultPhase = LifecyclePhase.PACKAGE,
      threadSafe = true,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME
    )
public class AssemblyMojo extends AbstractMojo {
    
    /**
     * Directory to stage assembly.
     *
     * @since 1.2.0
     */
    @Parameter(property = "stageDirectory", defaultValue = "${project.build.directory}/stage", required = true)
    protected File stageDirectory;
    
    /**
     * Directory to output assembly.
     *
     * @since 1.2.0
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;
    
    /**
     * Final name of assembly.
     *
     * @since 1.2.0
     */
    @Parameter(property = "finalName", defaultValue = "${project.artifactId}-${project.version}.tar.gz", required = true)
    protected String finalName;
    
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        if (!stageDirectory.exists()) {
            getLog().info("Creating stage directory: " + stageDirectory);
            stageDirectory.mkdirs();
        }
        
        // copy runtime dependencies to stage directory...
        //DependencyStatusSets dss = getDependencySets( this.failOnMissingClassifierArtifact, addParentPoms );
        //Set<Artifact> artifacts = dss.getResolvedDependencies();

        try {
            List<String> dependencies = project.getRuntimeClasspathElements();
            for (String d : dependencies) {
                getLog().info("Dependency: " + d);
            }
            
            // copy conf, bin, and share dirs
            File binDir = new File(project.getBasedir(), "bin");
            if (binDir.exists()) {
                File stageBinDir = new File(stageDirectory, "bin");
                FileUtils.copyDirectory(binDir, stageBinDir);
            }

            File confDir = new File(project.getBasedir(), "conf");
            if (confDir.exists()) {
                File stageConfDir = new File(stageDirectory, "conf");
                FileUtils.copyDirectory(confDir, stageConfDir);
            }

            File shareDir = new File(project.getBasedir(), "share");
            if (shareDir.exists()) {
                File stageShareDir = new File(stageDirectory, "share");
                FileUtils.copyDirectory(shareDir, stageShareDir);
            }

            // copy readme*, license*, changelog*, release* files
            FileUtils.copyDirectory(project.getBasedir(), stageDirectory, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName().toLowerCase();
                    if (name.startsWith("readme") || name.startsWith("changelog") || name.startsWith("release") || name.startsWith("license")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
}
