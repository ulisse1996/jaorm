package io.github.ulisse1996.jaorm.validation.mojo;

import io.github.ulisse1996.jaorm.tools.cache.FileHashCache;
import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.mojo.util.JaormMavenLogger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJaormMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true)
    private String jdbcDriver;

    @Parameter(required = true, readonly = true)
    private String jdbcUsername;

    @Parameter(required = true, readonly = true)
    private String jdbcPassword;

    @Parameter(required = true, readonly = true)
    private String jdbcUrl;

    @Parameter(readonly = true)
    protected boolean noCache;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    protected ConnectionInfo connectionInfo;
    protected FileHashCache cache;

    protected void init() throws IOException {
        this.connectionInfo = new ConnectionInfo(
                this.jdbcDriver,
                this.jdbcUrl,
                this.jdbcUsername,
                this.jdbcPassword
        );
        LogHolder.set(new JaormMavenLogger(getLog()));
        this.cache = FileHashCache.getInstance(project.getBasedir().getAbsolutePath());
    }

    protected List<String> getSources() {
        return project.getCompileSourceRoots();
    }

    protected ClassLoader generateClasspath() throws MojoExecutionException {
        try {
            List<String> classpathElements = project.getRuntimeClasspathElements();
            List<URL> projectClasspathList = new ArrayList<>();
            for (String element : classpathElements) {
                projectClasspathList.add(new File(element).toURI().toURL());
            }

            return new URLClassLoader(projectClasspathList.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }
}
