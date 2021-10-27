package io.github.ulisse1996.jaorm.validation.mojo;

import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.validation.log.LogHolder;
import io.github.ulisse1996.jaorm.validation.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.model.enums.ValidationType;
import io.github.ulisse1996.jaorm.validation.service.impl.ClasspathValidator;
import io.github.ulisse1996.jaorm.validation.service.impl.CombinedValidator;
import io.github.ulisse1996.jaorm.validation.service.impl.SourceValidator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mojo(name = "jaorm-validation", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class JaormValidationMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true)
    private String jdbcDriver;

    @Parameter(required = true, readonly = true)
    private String jdbcUsername;

    @Parameter(required = true, readonly = true)
    private String jdbcPassword;

    @Parameter(required = true, readonly = true)
    private String jdbcUrl;

    @Parameter(readonly = true, required = true)
    private ValidationType validationType;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            ConnectionInfo connectionInfo = new ConnectionInfo(
                    this.jdbcDriver,
                    this.jdbcUrl,
                    this.jdbcUsername,
                    this.jdbcPassword
            );
            LogHolder.set(createLogger());
            switch (this.validationType) {
                case CLASSPATH:
                    new ClasspathValidator(
                            generateClasspath(),
                            project.getBasedir().getAbsolutePath(),
                            connectionInfo
                    ).validate();
                    break;
                case SOURCE:
                    new SourceValidator(
                            getSources(),
                            project.getBasedir().getAbsolutePath(),
                            project.getArtifacts().stream()
                                    .map(Artifact::getFile)
                                    .collect(Collectors.toList()),
                            connectionInfo
                    ).validate();
                    break;
                case ALL:
                default:
                    new CombinedValidator(
                            new ClasspathValidator(
                                    generateClasspath(),
                                    project.getBasedir().getAbsolutePath(),
                                    connectionInfo
                            ),
                            new SourceValidator(
                                    getSources(),
                                    project.getBasedir().getAbsolutePath(),
                                    project.getArtifacts().stream()
                                            .map(Artifact::getFile)
                                            .collect(Collectors.toList()),
                                    connectionInfo
                            )
                    ).validate();
                    break;
            }
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoExecutionException(ex.getMessage(), ex);
        } finally {
            LogHolder.destroy();
        }
    }

    private JaormLogger createLogger() {
        return new JaormLogger() {
            @Override
            public void warn(Supplier<String> message) {
                getLog().warn(message.get());
            }

            @Override
            public void info(Supplier<String> message) {
                getLog().info(message.get());
            }

            @Override
            public void debug(Supplier<String> message) {
                getLog().debug(message.get());
            }

            @Override
            public void error(Supplier<String> message, Throwable throwable) {
                getLog().error(message.get(), throwable);
            }
        };
    }

    private List<String> getSources() {
        return project.getCompileSourceRoots();
    }

    private ClassLoader generateClasspath() throws MojoExecutionException {
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
