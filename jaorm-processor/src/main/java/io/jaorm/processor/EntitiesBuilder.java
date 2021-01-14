package io.jaorm.processor;

import com.squareup.javapoet.*;
import io.jaorm.QueryRunner;
import io.jaorm.entity.*;
import io.jaorm.processor.annotation.*;
import io.jaorm.processor.annotation.Relationship;
import io.jaorm.processor.exception.ProcessorException;
import io.jaorm.processor.util.Accessor;
import io.jaorm.processor.util.MethodUtils;
import io.jaorm.processor.util.ReturnTypeDefinition;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitiesBuilder {

    private final Set<TypeElement> entities;
    private ProcessingEnvironment processingEnv;

    public EntitiesBuilder(Set<TypeElement> entities) {
        this.entities = entities;
    }

    public void process(ProcessingEnvironment processingEnv) throws ProcessorException {
        this.processingEnv = processingEnv;
        for (TypeElement entity : entities) {
            String packageName = getPackage(entity);
            String delegate = entity.getSimpleName() + "Delegate";
            TypeSpec spec = build(entity, delegate);
            try {
                JavaFile file = JavaFile.builder(packageName, spec)
                        .indent("    ")
                        .skipJavaLangImports(true)
                        .build();
                file.writeTo(processingEnv.getFiler());
            } catch (IOException ex) {
                throw new ProcessorException(ex);
            }
        }
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private TypeSpec build(TypeElement entity, String delegateName) throws ProcessorException {
        TypeSpec.Builder builder = TypeSpec.classBuilder(ClassName.get(getPackage(entity), delegateName))
                .addModifiers(Modifier.PUBLIC)
                .superclass(entity.asType())
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(EntityDelegate.class), ClassName.get(entity)));
        TypeSpec columns = buildColumnEnum(entity);
        builder.addType(columns);
        builder.addField(ClassName.get(entity), "entity", Modifier.PRIVATE);
        builder.addMethods(buildDelegation(entity));
        builder.addMethods(buildOverrideEntity(entity));
        return builder.build();
    }

    private Iterable<MethodSpec> buildOverrideEntity(TypeElement entity) throws ProcessorException {
        MethodSpec supplierEntity = MethodSpec.overriding(MethodUtils.getMethod(processingEnv,"getEntityInstance", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(Supplier.class), ClassName.get(entity)))
                .addStatement("return $T::new", entity)
                .build();
        MethodSpec entityMapper = MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "getEntityMapper", EntityDelegate.class))
                .returns(ParameterizedTypeName.get(ClassName.get(EntityMapper.class), ClassName.get(entity)))
                .addStatement("return Column.getEntityMapper()")
                .build();
        MethodSpec setEntity = MethodSpec.overriding(MethodUtils.getMethod(processingEnv, "setEntity", EntityDelegate.class))
                .addStatement("this.entity = toEntity($L)", extractParameterNames(MethodUtils.getMethod(processingEnv, "setEntity", EntityDelegate.class)))
                .build();
        return Stream.of(supplierEntity, entityMapper, setEntity)
                .collect(Collectors.toList());
    }

    private TypeSpec buildColumnEnum(TypeElement entity) throws ProcessorException {
        List<Element> columns = entity.getEnclosedElements()
                .stream()
                .filter(ele -> ele.getAnnotation(Column.class) != null)
                .collect(Collectors.toList());
        List<Accessor> accessors = asAccessors(columns);
        TypeSpec.Builder builder = TypeSpec.enumBuilder("Column");
        builder.addField(getColumnGetterType(entity), "getter", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getColumnSetterType(entity), "setter", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getGenericClassType(), "type", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(String.class, "colName", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(boolean.class, "key", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(getEntityMapperType(entity), "ENTITY_MAPPER", Modifier.PRIVATE, Modifier.STATIC);
        builder.addMethod(addColumnConstructor(entity));
        for (Accessor accessor : accessors) {
            builder.addEnumConstant(accessor.getName(), enumImpl(accessor, entity));
        }
        builder.addMethod(
                MethodSpec.methodBuilder("getEntityMapper")
                .returns(getEntityMapperType(entity))
                .addModifiers(Modifier.STATIC, Modifier.SYNCHRONIZED)
                .addCode(buildEntityMapperCodeBlock())
                .build()
        );
        return builder.build();
    }

    private CodeBlock buildEntityMapperCodeBlock() {
        return CodeBlock.builder()
                .beginControlFlow("if (ENTITY_MAPPER == null)")
                .addStatement("$T.Builder builder = new $T.Builder()", EntityMapper.class, EntityMapper.class)
                .beginControlFlow("for (Column col : Column.values())")
                .addStatement("builder.add(col.colName, col.type, col.setter, col.getter, col.key)")
                .endControlFlow()
                .addStatement("ENTITY_MAPPER = builder.build()")
                .endControlFlow()
                .addStatement("return ENTITY_MAPPER")
                .build();
    }

    private TypeName getEntityMapperType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(EntityMapper.class), ClassName.get(entity));
    }

    private TypeSpec enumImpl(Accessor accessor, TypeElement entity) {
        return TypeSpec.anonymousClassBuilder("$T.class, $S, $T::$L, (entity, val) -> entity.$L(($T) val), $L",
                accessor.getGetterSetter().getKey().getReturnType(),
                accessor.getName(),
                ClassName.get(entity),
                accessor.getGetterSetter().getKey().getSimpleName(),
                accessor.getGetterSetter().getValue().getSimpleName(),
                accessor.getGetterSetter().getKey().getReturnType(),
                accessor.isKey()
                ).build();
    }

    private MethodSpec addColumnConstructor(TypeElement entity) {
        return MethodSpec.constructorBuilder()
                .addParameter(getGenericClassType(), "type")
                .addParameter(String.class, "colName")
                .addParameter(getColumnGetterType(entity), "getter")
                .addParameter(getColumnSetterType(entity), "setter")
                .addParameter(boolean.class, "key")
                .addStatement("this.getter = getter")
                .addStatement("this.setter = setter")
                .addStatement("this.colName = colName")
                .addStatement("this.type = type")
                .addStatement("this.key = key")
                .build();
    }

    private ParameterizedTypeName getGenericClassType() {
        return ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
    }

    private ParameterizedTypeName getColumnSetterType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(ColumnSetter.class), TypeName.get(entity.asType()), WildcardTypeName.subtypeOf(Object.class));
    }

    private ParameterizedTypeName getColumnGetterType(TypeElement entity) {
        return ParameterizedTypeName.get(ClassName.get(ColumnGetter.class), TypeName.get(entity.asType()), WildcardTypeName.subtypeOf(Object.class));
    }

    private List<Accessor> asAccessors(List<Element> columns) throws ProcessorException {
        List<Accessor> values = new ArrayList<>();
        for (Element column : columns) {
            boolean key = column.getAnnotation(Id.class) != null;
            ExecutableElement getter = findGetter(column);
            ExecutableElement setter = findSetter(column);
            values.add(new Accessor(column.getAnnotation(Column.class).name(), new AbstractMap.SimpleEntry<>(getter, setter), key));
        }

        return values;
    }

    private Iterable<MethodSpec> buildDelegation(TypeElement entity) throws ProcessorException {
        List<? extends Element> elements = entity.getEnclosedElements();
        List<? extends ExecutableElement> methods = elements.stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(ExecutableElement.class::cast)
                .filter(e -> !e.getKind().equals(ElementKind.CONSTRUCTOR))
                .collect(Collectors.toList());
        List<ExecutableElement> joins = new ArrayList<>();
        for (Element element : elements) {
            if (hasJoinAnnotation(element)) {
                ExecutableElement getter = findGetter(element);
                joins.add(getter);
            }
        }
        return methods.stream()
                .map(m -> {
                    if (joins.contains(m)) {
                        return buildJoinMethod(m);
                    } else {
                        return buildDelegateMethod(m);
                    }
                }).collect(Collectors.toList());
    }

    private MethodSpec buildDelegateMethod(ExecutableElement m) {
        MethodSpec.Builder builder = MethodSpec.overriding(m)
                .addStatement("$T.requireNonNull(this.entity)", Objects.class);
        String variables;
        variables = extractParameterNames(m);

        if (m.getReturnType() instanceof NoType) {
            builder.addStatement("this.entity.$L($L)", m.getSimpleName(), variables);
        } else {
            builder.addStatement("return this.entity.$L($L)", m.getSimpleName(), variables);
        }
        return builder.build();
    }

    private String extractParameterNames(ExecutableElement m) {
        if (!m.getParameters().isEmpty()) {
            return m.getParameters().stream()
                    .map(VariableElement::getSimpleName)
                    .collect(Collectors.joining(","));
        }

        return "";
    }

    private MethodSpec buildJoinMethod(ExecutableElement method) {
        ReturnTypeDefinition definition = new ReturnTypeDefinition(processingEnv, method.getReturnType());
        String runnerMethod;
        if (definition.isCollection()) {
            runnerMethod = "readAll";
        } else if (definition.isOptional()) {
            runnerMethod = "readOpt";
        } else {
            runnerMethod = "read";
        }

        // TODO Add arguments
        return MethodSpec.overriding(method)
                .addStatement("return $T.$L($T.class, $T.getCurrent().getSql($T.class), null)",
                        QueryRunner.class, runnerMethod, definition.getRealClass(), DelegatesService.class,
                        definition.getRealClass())
                .build();
    }

    private ExecutableElement findGetter(Element element) throws ProcessorException {
        String fieldName = element.getSimpleName().toString();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String getter = "get" + fieldName;
        return findExecutable(element, getter, "Can't find getter for field " + element.getSimpleName());
    }

    private ExecutableElement findSetter(Element element) throws ProcessorException {
        String fieldName = element.getSimpleName().toString();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String setter = "set" + fieldName;
        return findExecutable(element, setter, "Can't find setter for field " + element.getSimpleName());
    }

    private ExecutableElement findExecutable(Element element, String name, String errorMessage) throws ProcessorException {
        return element.getEnclosingElement()
                .getEnclosedElements()
                .stream()
                .filter(ele -> ele.getSimpleName().contentEquals(name))
                .findFirst()
                .map(ExecutableElement.class::cast)
                .orElseThrow(() -> new ProcessorException(errorMessage));
    }

    private boolean hasJoinAnnotation(Element ele) {
        return ele.getAnnotation(Relationship.class) != null;
    }
}
