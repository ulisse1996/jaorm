package io.jaorm.processor;

import com.squareup.javapoet.*;
import io.jaorm.BaseDao;
import io.jaorm.QueryRunner;
import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.processor.annotation.Query;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.strategy.QueryStrategy;
import io.jaorm.processor.util.MethodUtils;
import io.jaorm.processor.util.ReturnTypeDefinition;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueriesBuilder {

    private final Set<TypeElement> types;
    private final ProcessingEnvironment processingEnvironment;

    public QueriesBuilder(ProcessingEnvironment processingEnvironment, Set<TypeElement> types) {
        this.types = types;
        this.processingEnvironment = processingEnvironment;
    }

    public void process() {
        for (TypeElement query : types) {
            Set<MethodSpec> methods = new HashSet<>();
            for (Element ele : query.getEnclosedElements()) {
                if (ele.getAnnotation(Query.class) != null) {
                    ExecutableElement executableElement = (ExecutableElement) ele;
                    MethodSpec methodSpec = buildImpl(executableElement);
                    methods.add(methodSpec);
                }
            }

            List<AnnotationSpec> annotations = getExtraAnnotations(query);

            if (isBaseDao(query)) {
                methods.addAll(buildBaseDao(query));
            }

            String packageName = getPackage(query);
            TypeSpec build = TypeSpec.classBuilder(query.getSimpleName() + "Impl")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotations(annotations)
                    .addSuperinterface(query.asType())
                    .addMethods(methods)
                    .build();
            try {
                JavaFile file = JavaFile.builder(packageName, build)
                        .indent("    ")
                        .skipJavaLangImports(true)
                        .build();
                file.writeTo(processingEnvironment.getFiler());
            } catch (IOException ex) {
                throw new ProcessorException(ex);
            }
        }
    }

    private List<AnnotationSpec> getExtraAnnotations(TypeElement query) {
        return query.getAnnotationMirrors()
                .stream()
                .map(AnnotationSpec::get)
                .collect(Collectors.toList());
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private Collection<MethodSpec> buildBaseDao(TypeElement query) {
        String realClass = getBaseDaoGeneric(query);
        ClassName className = ClassName.bestGuess(realClass);
        MethodSpec read = MethodSpec.overriding(MethodUtils.getMethod(processingEnvironment, "read", BaseDao.class))
                .returns(className)
                .addStatement("return $T.getInstance($T.class).read($T.class, $T.getCurrent().getSql($T.class), argumentsAsParameters($L.getValues()))",
                        QueryRunner.class, className, className, DelegatesService.class, className, "arg0")
                .build();
        MethodSpec readOpt = MethodSpec.overriding(MethodUtils.getMethod(processingEnvironment, "readOpt", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), className))
                .addStatement("return $T.getInstance($T.class).readOpt($T.class, $T.getCurrent().getSql($T.class), argumentsAsParameters($L.getValues()))",
                        QueryRunner.class, className, className, DelegatesService.class, className, "arg0")
                .build();
        MethodSpec readAll = MethodSpec.overriding(MethodUtils.getMethod(processingEnvironment, "readAll", BaseDao.class))
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), className))
                .addStatement("return $T.getInstance($T.class).readAll($T.class, $T.getCurrent().getSql($T.class), argumentsAsParameters($L.getValues()))",
                        QueryRunner.class, className, className, DelegatesService.class, className, "arg0")
                .build();
        MethodSpec update = MethodSpec.overriding(MethodUtils.getMethod(processingEnvironment, "update", BaseDao.class))
                .addStatement("Object[] merged = $T.of(arg0, arg1).toArray()", Stream.class)
                .addStatement("$T.getInstance($T.class).update($T.getCurrent().getSql($T.class), argumentsAsParameters(merged))",
                        QueryRunner.class, className, DelegatesService.class, className)
                .build();
        MethodSpec delete = MethodSpec.overriding(MethodUtils.getMethod(processingEnvironment, "delete", BaseDao.class))
                .addStatement("$T.getInstance($T.class).delete($T.getCurrent().getSql($T.class), argumentsAsParameters($L.getValues()))",
                        QueryRunner.class, className, DelegatesService.class, className, "arg0")
                .build();
        return Stream.of(read, readOpt, readAll, update, delete).collect(Collectors.toList());
    }

    private String getBaseDaoGeneric(TypeElement query) {
        for (TypeMirror typeMirror : query.getInterfaces()) {
            if (typeMirror.toString().contains(BaseDao.class.getName())) {
                return typeMirror.toString().replace(BaseDao.class.getName(), "")
                        .replace("<", "")
                        .replace(">", "");
            }
        }

        throw new ProcessorException("Can't find generic type of BaseDao");
    }

    private boolean isBaseDao(TypeElement query) {
        if (!query.getInterfaces().isEmpty()) {
            for (TypeMirror typeMirror : query.getInterfaces()) {
                if (typeMirror.toString().contains(BaseDao.class.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    private MethodSpec buildImpl(ExecutableElement executableElement) {
        Query query = executableElement.getAnnotation(Query.class);
        String sql = query.sql();
        MethodSpec.Builder builder = MethodSpec.overriding(executableElement)
                .addStatement("$T params = new $T<>()",
                        ParameterizedTypeName.get(List.class, SqlParameter.class), ArrayList.class);
        for (QueryStrategy queryStrategy : QueryStrategy.values()) {
            if (queryStrategy.isValid(sql)) {
                int paramNumber = queryStrategy.getParamNumber(sql);
                if (paramNumber != executableElement.getParameters().size()) {
                    throw new ProcessorException("Mismatch between parameters and query parameters for method " + executableElement);
                }
                List<CodeBlock> statements = queryStrategy.extract(this.processingEnvironment, sql, executableElement);
                statements.forEach(builder::addCode);
                sql = queryStrategy.replaceQuery(sql);
                Map.Entry<String, Object[]> checked = checkType(sql, executableElement);
                builder.addStatement(checked.getKey(), checked.getValue());
                return builder.build();
            }
        }

        throw new ProcessorException("Can't find query strategy");
    }

    private Map.Entry<String, Object[]> checkType(String sql, ExecutableElement method) {
        if (sql.toUpperCase().startsWith("SELECT")) {
            return checkSelect(sql, method);
        } else if (sql.toUpperCase().startsWith("DELETE")) {
            assertVoid(method);
            return new AbstractMap.SimpleImmutableEntry<>("$T.getSimple().delete($S, params)",
                    new Object[] {QueryRunner.class, sql});
        } else if (sql.toUpperCase().startsWith("UPDATE")) {
            assertVoid(method);
            return new AbstractMap.SimpleImmutableEntry<>("$T.getSimple().update($S, params)",
                    new Object[] {QueryRunner.class, sql});
        }

        throw new ProcessorException(String.format("Operation not supported for sql [%s] in method [%s]", sql, method));
    }

    private void assertVoid(ExecutableElement method) {
        if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
            throw new ProcessorException("Can't use Delete or Update statement with a non-void method");
        }
    }

    private Map.Entry<String, Object[]> checkSelect(String sql, ExecutableElement method) {
        if (method.getReturnType().getKind().equals(TypeKind.VOID)) {
            throw new ProcessorException(String.format("Select of method %s need a return type !", method));
        } else {
            ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnvironment, method.getReturnType());
            Object[] stmParams = {
                    QueryRunner.class, definition.getRealClass(), definition.getRealClass(), sql
            };
            if (definition.isOptional()) {
                return new AbstractMap.SimpleImmutableEntry<>(
                        "return $T.getInstance($T.class).readOpt($T.class, $S, params)",
                        stmParams
                );
            } else if (definition.isCollection()) {
                return new AbstractMap.SimpleImmutableEntry<>(
                        "return $T.getInstance($T.class).readAll($T.class, $S, params)",
                        stmParams
                );
            } else {
                return new AbstractMap.SimpleImmutableEntry<>(
                        "return $T.getInstance($T.class).read($T.class, $S, params)",
                        stmParams
                );
            }
        }
    }
}
