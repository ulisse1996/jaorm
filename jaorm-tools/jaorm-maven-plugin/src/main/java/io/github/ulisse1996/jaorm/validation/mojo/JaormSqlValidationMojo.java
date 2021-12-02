package io.github.ulisse1996.jaorm.validation.mojo;

import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.enums.ValidationType;
import io.github.ulisse1996.jaorm.tools.service.impl.ClasspathValidator;
import io.github.ulisse1996.jaorm.tools.service.impl.CombinedValidator;
import io.github.ulisse1996.jaorm.tools.service.impl.SourceValidator;
import io.github.ulisse1996.jaorm.validation.mojo.util.JaormMavenLogger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@Mojo(name = "jaorm-sql-validation", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class JaormSqlValidationMojo extends AbstractJaormMojo {

    @Parameter(readonly = true, required = true)
    private ValidationType validationType;

    @Override
    public void execute() throws MojoExecutionException {
        LogHolder.set(new JaormMavenLogger(getLog()));
        try {
            init();
            switch (validationType) {
                case CLASSPATH:
                    new ClasspathValidator(
                            generateClasspath(),
                            project.getBasedir().getAbsolutePath(),
                            connectionInfo,
                            this.noCache
                    ).validateQueries();
                    break;
                case SOURCE:
                    new SourceValidator(
                            getSources(),
                            project.getBasedir().getAbsolutePath(),
                            project.getArtifacts().stream()
                                    .map(Artifact::getFile)
                                    .collect(Collectors.toList()),
                            connectionInfo,
                            this.noCache
                    ).validateQueries();
                    break;
                case ALL:
                default:
                    new CombinedValidator(
                            new ClasspathValidator(
                                    generateClasspath(),
                                    project.getBasedir().getAbsolutePath(),
                                    connectionInfo,
                                    this.noCache
                            ),
                            new SourceValidator(
                                    getSources(),
                                    project.getBasedir().getAbsolutePath(),
                                    project.getArtifacts().stream()
                                            .map(Artifact::getFile)
                                            .collect(Collectors.toList()),
                                    connectionInfo,
                                    this.noCache
                            )
                    ).validateQueries();
                    break;
            }
            cache.saveOnFile();
        } catch (IOException | QueryValidationException | NoSuchAlgorithmException e) {
            LogHolder.get().error(e::getMessage, e);
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            LogHolder.destroy();
        }
    }
}
