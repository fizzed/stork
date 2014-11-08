package co.fizzed.stork.maven;

import co.fizzed.stork.util.AssemblyUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
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
    @Parameter(property = "stageDirectory", defaultValue = "${project.build.directory}/stork", required = true)
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
    @Parameter(property = "finalName", defaultValue = "${project.build.finalName}", required = true)
    protected String finalName;
    
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!stageDirectory.exists()) {
            getLog().info("Creating stage directory: " + stageDirectory);
            stageDirectory.mkdirs();
        }

        try {
            List<Artifact> artifacts = artifactsToStage();
            
            //
            // copy runtime dependencies to stage directory...
            //
            File stageLibDir = new File(stageDirectory, "lib");
            
            // directly pulled from maven Project.java (how it returns the getRuntimeClasspathElements() value)
            for (Artifact a : artifacts) {
                File f = a.getFile();
                if (f == null) {
                    getLog().error("Project artifact was null (maybe not compiled into jar yet?)");
                } else {
                    // generate final jar name (which appends groupId)
                    String artifactName = a.getGroupId() + "." + a.getArtifactId() + "-" + a.getVersion() + ".jar";
                    File stageArtificateFile = new File(stageLibDir, artifactName);
                    FileUtils.copyFile(f, stageArtificateFile);
                }
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

            // copy standard project resources (e.g. readme*, license*, changelog*, release* files)
            AssemblyUtils.copyStandardProjectResources(project.getBasedir(), stageDirectory);
            
            // tarball it up
            File tgzFile = AssemblyUtils.createTGZ(outputDirectory, stageDirectory, finalName);
            getLog().info("Generated maven stork assembly: " + tgzFile);
            
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    public List<Artifact> artifactsToStage() {
        List<Artifact> artifacts = new ArrayList<Artifact>();
        
        // include project artifact?
        if (!shouldArtifactBeStaged(project.getArtifact())) {
            getLog().info("Project artifact may have a classifier or is not of type jar (will not be staged)");
        } else {
            artifacts.add(project.getArtifact());
        }
        
        // any additional artifacts attached to this project?
        for (Artifact a : project.getAttachedArtifacts()) {
             if (shouldArtifactBeStaged(a)) {
                 artifacts.add(a);
             }
        }
        
        // get resolved artifacts as well
        for (Artifact a : project.getArtifacts()) {
            if (a.getArtifactHandler().isAddedToClasspath() && (Artifact.SCOPE_COMPILE.equals(a.getScope()) || Artifact.SCOPE_RUNTIME.equals(a.getScope()))) {
                artifacts.add(a);
            }
        }

        return artifacts;
    }
    
    public boolean shouldArtifactBeStaged(Artifact a) {
        return !a.hasClassifier() && (a.getType() == null || a.getType().equalsIgnoreCase("jar"));
    }
    
}
