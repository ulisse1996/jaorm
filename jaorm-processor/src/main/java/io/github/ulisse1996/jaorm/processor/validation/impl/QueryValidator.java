package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.ExcludeExternalValidation;
import io.github.ulisse1996.jaorm.annotation.Id;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.processor.ExtensionLoader;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.strategy.QueryStrategy;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.validation.Validator;
import io.github.ulisse1996.jaorm.specialization.DoubleKeyDao;
import io.github.ulisse1996.jaorm.specialization.SingleKeyDao;
import io.github.ulisse1996.jaorm.specialization.TripleKeyDao;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryValidator extends Validator {

    private static final List<SpecializationSpecific> SPECIALIZED_DAOS = Arrays.asList(
            new SpecializationSpecific(SingleKeyDao.class, 1),
            new SpecializationSpecific(DoubleKeyDao.class, 2),
            new SpecializationSpecific(TripleKeyDao.class, 3)
    );
    private final List<ExtensionLoader.ExtensionManager> extensions;

    public QueryValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
        this.extensions = ExtensionLoader.getInstance(Thread.currentThread().getContextClassLoader())
                .loadValidationExtensions(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        List<ExecutableElement> queries = annotated.stream()
                .filter(ele -> ele.getAnnotation(Query.class) != null)
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
        queries.forEach(this::checkQuery);
        annotated.stream()
                .filter(ele -> ele.getAnnotation(Dao.class) != null)
                .map(TypeElement.class::cast)
                .forEach(this::checkKeys);
    }

    private void checkKeys(TypeElement type) {
        debugMessage("Check number of keys for Query " + type.getSimpleName());
        for (SpecializationSpecific specific : SPECIALIZED_DAOS) {
            Optional<TypeElement> entity = findOptSpecific(type, specific);
            if (entity.isPresent()) {
                long keys = findKeysNumber(entity.get());
                if (keys != specific.parameters) {
                    throw new ProcessorException(String.format("Error on %s ! Required %d @Id but found %d in Entity",
                            type.getSimpleName(), specific.parameters, keys));
                }
            }
        }
    }

    private long findKeysNumber(TypeElement typeElement) {
        return ProcessorUtils.getAllValidElements(processingEnvironment, typeElement)
                .stream()
                .filter(el -> el.getAnnotation(Id.class) != null)
                .count();
    }

    private Optional<TypeElement> findOptSpecific(TypeElement type, SpecializationSpecific specific) {
        if (ProcessorUtils.isSubType(this.processingEnvironment, type, specific.klass)) {
            String t = ProcessorUtils.getBaseDaoGeneric(processingEnvironment, type);
            return Optional.of(this.processingEnvironment.getElementUtils().getTypeElement(t));
        }

        return Optional.empty();
    }

    private void checkQuery(ExecutableElement executableElement) {
        debugMessage("Check validation for Query " + executableElement.getSimpleName());
        Query query = executableElement.getAnnotation(Query.class);
        String sql = getSql(query);
        for (QueryStrategy queryStrategy : QueryStrategy.values()) {
            if (queryStrategy.isValid(sql, query.noArgs())) {
                int paramNumber = queryStrategy.getParamNumber(sql);
                if (paramNumber != executableElement.getParameters().size()) {
                    throw mismatchParameters(queryStrategy, executableElement, sql);
                }
                checkSpecs(sql, executableElement);
                if (executableElement.getAnnotation(ExcludeExternalValidation.class) == null) {
                    for (ExtensionLoader.ExtensionManager extension : extensions) {
                        extension.executeValidation(queryStrategy.replaceQuery(sql, Collections.emptySet()), processingEnvironment);
                    }
                } else {
                    debugMessage(String.format("Skipping sql %s from external validation", sql));
                }

                return;
            }
        }

        throw new ProcessorException(String.format("Can't find query strategy for method %s", executableElement.getSimpleName()));
    }

    private ProcessorException mismatchParameters(QueryStrategy queryStrategy, ExecutableElement executableElement, String sql) {
        if (queryStrategy.getRegex() != null) {
            Set<String> words = new HashSet<>(QueryStrategy.getWords(queryStrategy.getRegex(), sql));
            List<String> params = executableElement.getParameters().stream()
                    .map(VariableElement::getSimpleName)
                    .map(Name::toString)
                    .collect(Collectors.toList());
            return new ProcessorException(String.format("Mismatch between parameters and query parameters for methods %s! Found: %s, required %s",
                    executableElement.getSimpleName(), words, params));
        } else {
            int paramNumber = queryStrategy.getParamNumber(sql);
            return new ProcessorException(String.format("Mismatch between parameters and query parameters for method %s! Found: %d, required %d",
                    executableElement.getSimpleName(), executableElement.getParameters().size(), paramNumber));
        }
    }

    private String getSql(Query query) {
        String sql = query.sql();
        return ProcessorUtils.getSqlOrSqlFromFile(sql, this.processingEnvironment);
    }

    private void checkSpecs(String sql, ExecutableElement method) {
        if (isValidSelect(sql)) {
            checkReturnMethod(method);
            return;
        } else if (sql.toUpperCase().startsWith("DELETE") || sql.toUpperCase().startsWith("UPDATE")) {
            assertVoid(method);
            return;
        }

        throw new ProcessorException(String.format("Operation not supported for sql %s in method %s", sql, method));
    }

    private boolean isValidSelect(String sql) {
        return sql.toUpperCase().startsWith("SELECT") || sql.toUpperCase().startsWith("WITH");
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

    private static class SpecializationSpecific {

        private final int parameters;
        private final Class<?> klass;

        private SpecializationSpecific(Class<?> klass, int parameters) {
            this.klass = klass;
            this.parameters = parameters;
        }
    }
}
