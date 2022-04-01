package io.github.ulisse1996.jaorm.processor;

import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.generation.GenerationType;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.validation.Validator;
import io.github.ulisse1996.jaorm.processor.validation.ValidatorType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.github.ulisse1996.jaorm.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("jaorm.tables.suffix")
public class JaormProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ConfigHolder.init(processingEnv.getOptions());
        initServices();
        validate(roundEnv);
        generate(roundEnv);
        ConfigHolder.destroy();
        return true;
    }

    private void initServices() {
        try {
            Optional<FileObject> resource = getOrCreateGenerated();
            if (resource.isPresent()) {
                URI uri = resource.get().toUri();
                // For test we use memory filesystem, so we need to skip this set
                if (isTestInMemory(uri.toString())) {
                    return;
                }
                ConfigHolder.setServices(Paths.get(uri).getParent());
                Files.deleteIfExists(Paths.get(uri));
            }
        } catch (IOException ex) {
            throw new ProcessorException("Can't init services folder", ex);
        }
    }

    private boolean isTestInMemory(String s) {
        return s.startsWith("mem:");
    }

    private Optional<FileObject> getOrCreateGenerated() throws IOException {
        try {
            return Optional.of(this.processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/jaorm_generated"));
        } catch (FilerException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Attempt to reopen")) {
                // We have resource, we just save current location
                String message = ex.getMessage().replace("Attempt to reopen a file for path", "")
                        .trim()
                        .replace("META-INF/services/jaorm_generated", "");
                ConfigHolder.setServices(Paths.get(message));
                return Optional.empty();
            }

            throw ex;
        }
    }

    private void generate(RoundEnvironment roundEnv) {
        for (GenerationType type : GenerationType.values()) {
            Generator.forType(type, processingEnv)
                    .generate(roundEnv);
        }
    }

    private void validate(RoundEnvironment roundEnv) {
        for (ValidatorType type : ValidatorType.values()) {
            List<? extends Element> annotated = type.getSupported()
                    .stream()
                    .flatMap(an -> roundEnv.getElementsAnnotatedWith(an).stream())
                    .collect(Collectors.toList());
            Validator.forType(type, processingEnv)
                    .validate(annotated);
        }
    }
}
