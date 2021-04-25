package io.jaorm.processor.validation.impl;

import io.jaorm.annotation.Query;
import io.jaorm.logger.JaormLogger;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.strategy.QueryStrategy;
import io.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import java.util.List;
import java.util.stream.Collectors;

public class QueryValidator extends Validator {

    private static final JaormLogger logger = JaormLogger.getLogger(QueryValidator.class);

    public QueryValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        List<ExecutableElement> queries = annotated.stream()
                .filter(ele -> ele.getAnnotation(Query.class) != null)
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
        queries.forEach(this::checkQuery);
    }

    private void checkQuery(ExecutableElement executableElement) {
        logger.debug(() -> "Check validation for Query " + executableElement.getSimpleName());
        Query query = executableElement.getAnnotation(Query.class);
        String sql = query.sql();
        for (QueryStrategy queryStrategy : QueryStrategy.values()) {
            if (queryStrategy.isValid(sql, query.noArgs())) {
                int paramNumber = queryStrategy.getParamNumber(sql);
                if (paramNumber != executableElement.getParameters().size()) {
                    throw new ProcessorException("Mismatch between parameters and query parameters for method " + executableElement.getSimpleName());
                }
                checkSpecs(sql, executableElement);
                return;
            }
        }

        throw new ProcessorException(String.format("Can't find query strategy for method %s", executableElement.getSimpleName()));
    }

    private void checkSpecs(String sql, ExecutableElement method) {
        if (sql.toUpperCase().startsWith("SELECT")) {
            checkReturnMethod(method);
            return;
        } else if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("UPDATE")) {
            assertVoid(method);
            return;
        }

        throw new ProcessorException(String.format("Operation not supported for sql %s in method %s", sql, method));
    }

    private void checkReturnMethod(ExecutableElement method) {
        if (method.getReturnType().getKind().equals(TypeKind.VOID)) {
            throw new ProcessorException("Can't use Select statement with a void method");
        }
    }

    private void assertVoid(ExecutableElement method) {
        if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
            throw new ProcessorException("Can't use Delete or Update statement with a non-void method");
        }
    }
}
