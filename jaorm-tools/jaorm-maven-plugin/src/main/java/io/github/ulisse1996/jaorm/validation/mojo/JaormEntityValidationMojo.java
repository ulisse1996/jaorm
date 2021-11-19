package io.github.ulisse1996.jaorm.validation.mojo;

import io.github.ulisse1996.jaorm.tools.logger.LogHolder;
import io.github.ulisse1996.jaorm.tools.model.enums.ValidationType;
import io.github.ulisse1996.jaorm.tools.service.impl.ClasspathValidator;
import io.github.ulisse1996.jaorm.tools.service.impl.CombinedValidator;
import io.github.ulisse1996.jaorm.tools.service.impl.SourceValidator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.stream.Collectors;

@Mojo(name = "jaorm-validation", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class JaormEntityValidationMojo extends AbstractJaormMojo {

    @Parameter(readonly = true, required = true)
    private ValidationType validationType;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            init();
            switch (this.validationType) {
                case CLASSPATH:
                    new ClasspathValidator(
                            generateClasspath(),
                            project.getBasedir().getAbsolutePath(),
                            connectionInfo,
                            this.noCache
                    ).validateEntities();
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
                    ).validateEntities();
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
                    ).validateEntities();
                    break;
            }
            cache.saveOnFile();
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoExecutionException(ex.getMessage(), ex);
        } finally {
            LogHolder.destroy();
        }
    }
}
