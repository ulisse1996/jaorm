package io.github.ulisse1996.jaorm.processor.strategy;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Set;

public interface ParametersStrategy {

    boolean isValid(String query, boolean noArgs);
    int getParamNumber(String query);
    GenerationUnit extract(ProcessingEnvironment procEnv, String query, ExecutableElement method);
    String replaceQuery(String query, Set<String> collectionNames);
}
