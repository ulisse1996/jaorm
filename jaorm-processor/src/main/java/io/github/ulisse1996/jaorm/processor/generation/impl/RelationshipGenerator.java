package io.github.ulisse1996.jaorm.processor.generation.impl;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;
import io.github.ulisse1996.jaorm.entity.relationship.EntityEventType;
import io.github.ulisse1996.jaorm.entity.relationship.Relationship;
import io.github.ulisse1996.jaorm.entity.relationship.RelationshipManager;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.generation.Generator;
import io.github.ulisse1996.jaorm.processor.util.GeneratedFile;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.util.ReturnTypeDefinition;
import io.github.ulisse1996.jaorm.spi.provider.RelationshipProvider;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationshipGenerator extends Generator {

    private static final List<String> INCREMENTAL_JPS = Collections.singletonList("org.jetbrains.jps.incremental");

    public RelationshipGenerator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void generate(RoundEnvironment roundEnvironment) {
        List<TypeElement> entities = roundEnvironment.getElementsAnnotatedWith(Table.class)
                .stream()
                .map(TypeElement.class::cast)
                .collect(Collectors.toList());
        List<TypeElement> queries = ProcessorUtils.getAllDao(roundEnvironment);
        if (entities.isEmpty()) {
            return;
        }

        List<RelationshipInfo> infos = new ArrayList<>();
        Set<TypeElement> daoTypes = queries.stream()
                .filter(e -> ProcessorUtils.isBaseDao(processingEnvironment, e))
                .map(e -> ProcessorUtils.getBaseDaoGeneric(processingEnvironment, e))
                .map(processingEnvironment.getElementUtils()::getTypeElement)
                .collect(Collectors.toSet());
        for (TypeElement entity : entities) {
            if (hasRelationships(entity)) {
                checkBaseDao(entity, daoTypes);
                infos.add(buildRelationship(entity, daoTypes));
            }
        }

        if (!infos.isEmpty()) {
            generateRelationships(infos);
        }
    }

    private RelationshipInfo buildRelationship(TypeElement entity, Set<TypeElement> daoTypes) {
        List<RelationshipAccessor> annotated = processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .filter(el -> el.getAnnotation(Cascade.class) != null)
                .filter(el -> el.getAnnotation(io.github.ulisse1996.jaorm.annotation.Relationship.class) != null)
                .sorted(Comparator.comparing(el -> el.getAnnotation(io.github.ulisse1996.jaorm.annotation.Relationship.class).priority()))
                .map(el -> {
                    ExecutableElement getter = ProcessorUtils.findGetter(processingEnvironment, entity, el.getSimpleName());
                    ReturnTypeDefinition returnTypeDefinition = new ReturnTypeDefinition(processingEnvironment, getter.getReturnType());
                    CascadeType[] cascadeTypes = el.getAnnotation(Cascade.class).value();
                    return new RelationshipAccessor(returnTypeDefinition, getter, cascadeTypes, (VariableElement) el);
                }).collect(Collectors.toList());
        annotated.forEach(acc -> checkBaseDao(acc.returnTypeDefinition.getRealClass(), daoTypes));
        return new RelationshipInfo(entity, annotated);
    }

    private void generateRelationships(List<RelationshipInfo> relationshipInfos) {
        List<GeneratedFile> files = new ArrayList<>();
        for (RelationshipInfo info : relationshipInfos) {
            TypeSpec relationshipEvents = TypeSpec.classBuilder(String.format("%sRelationshipsProvider", info.entity.getSimpleName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(RelationshipProvider.class)
                    .addMethods(getProviderMethods(info))
                    .build();
            GeneratedFile file = new GeneratedFile(getPackage(info.entity), relationshipEvents, "");
            ProcessorUtils.generate(processingEnvironment, file);
            files.add(file);
        }

        ProcessorUtils.generateSpi(processingEnvironment, files, RelationshipProvider.class);
    }

    private Iterable<MethodSpec> getProviderMethods(RelationshipInfo info) {
        MethodSpec getEntityClass = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getEntityClass", RelationshipProvider.class))
                .addStatement("return $T.class", info.entity)
                .build();

        MethodSpec.Builder builder = MethodSpec.overriding(ProcessorUtils.getMethod(processingEnvironment, "getRelationship", RelationshipProvider.class))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addStatement("$T rel = new $T<>($T.class)", Relationship.class, Relationship.class, info.entity);

        for (RelationshipAccessor relationship : info.annotated) {
            if (relationship.returnTypeDefinition.isCursor()) {
                continue;
            }

            String events;
            EntityEventType[] values = getEvents(relationship);
            events = asVarArgs(values);
            builder.addStatement("rel.add(new $T<>($T.class, e -> (($T)e).$L(), $L, $L, $S, $L, $L))",
                    Relationship.Node.class,
                    relationship.returnTypeDefinition.getRealClass(),
                    info.entity,
                    relationship.getter.getSimpleName(),
                    relationship.returnTypeDefinition.isOptional() ? "true" : "false",
                    relationship.returnTypeDefinition.isCollection() ? "true" : "false",
                    relationship.relationship.getSimpleName(),
                    getKeys(info.entity, relationship),
                    events
            );
            addAutoSetNode(info.entity, relationship, builder);
        }

        return Arrays.asList(getEntityClass, builder.addStatement("return rel").build());
    }

    private String getKeys(TypeElement entity, RelationshipAccessor relationship) {
        io.github.ulisse1996.jaorm.annotation.Relationship currRel = relationship.relationship.getAnnotation(io.github.ulisse1996.jaorm.annotation.Relationship.class);
        Set<String> relColumns = Stream.of(currRel.columns())
                .map(io.github.ulisse1996.jaorm.annotation.Relationship.RelationshipColumn::sourceColumn)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        List<String> entityKeys = ProcessorUtils.getAllValidElements(processingEnvironment, entity)
                .stream()
                .filter(el -> el.getKind().isField())
                .filter(el -> el.getAnnotation(Id.class) != null)
                .map(el -> el.getAnnotation(Column.class).name())
                .collect(Collectors.toList());
        List<String> keys = new ArrayList<>();
        for (String key : entityKeys) {
            if (relColumns.contains(key)) {
                keys.add(key);
            }
        }
        if (keys.size() == 0) {
            return String.format("%s.emptyList()", Collections.class.getName());
        } else if (keys.size() == 1) {
            return String.format("%s.singletonList(\"%s\")", Collections.class.getName(), keys.get(0));
        } else {
            return String.format("%s.asList(%s)", Arrays.class.getName(),
                    keys.stream().map(el -> String.format("\"%s\"", el)).collect(Collectors.joining(",")));
        }
    }

    private String getPackage(TypeElement entity) {
        return ClassName.get(entity).packageName();
    }

    private void addAutoSetNode(TypeElement entity, RelationshipAccessor relationship, MethodSpec.Builder builder) {
        TypeElement rel = relationship.returnTypeDefinition.getRealClass();
        VariableElement variable = relationship.relationship;
        io.github.ulisse1996.jaorm.annotation.Relationship currRel = variable.getAnnotation(io.github.ulisse1996.jaorm.annotation.Relationship.class);
        RelationshipStatements params = new RelationshipStatements();
        for (io.github.ulisse1996.jaorm.annotation.Relationship.RelationshipColumn column : currRel.columns()) {
            VariableElement toSet = ProcessorUtils.getFieldWithColumnName(processingEnvironment, rel, column.targetColumn());
            if (column.sourceColumn().isEmpty()) {
                if (toSet.getAnnotation(Converter.class) == null) {
                    params.put(
                            "$T.applyCallback($S, null, link, null, $T.class, $T::$L, null, $T.$L.toValue($S))",
                            Arrays.asList(
                                    RelationshipManager.class,
                                    String.format("%s -> %s", entity.getSimpleName(), rel.getSimpleName()),
                                    rel,
                                    rel,
                                    ProcessorUtils.findSetter(processingEnvironment, rel, toSet.getSimpleName()).getSimpleName(),
                                    ParameterConverter.class,
                                    column.converter().name(),
                                    column.defaultValue()
                            )
                    );
                } else {
                    params.put(
                            "(($T)link).$L($L.fromSql($T.$L.toValue($S)))",
                            Arrays.asList(
                                    rel,
                                    ProcessorUtils.findSetter(processingEnvironment, rel, toSet.getSimpleName()).getSimpleName(),
                                    ProcessorUtils.getConverterCaller(processingEnvironment, toSet),
                                    ParameterConverter.class,
                                    column.converter().name(),
                                    column.defaultValue()
                            ));
                }
            } else {
                VariableElement fromSet = ProcessorUtils.getFieldWithColumnName(processingEnvironment, entity, column.sourceColumn());
                if (fromSet.getAnnotation(Converter.class) == null || noNeedForConversion(fromSet, toSet)) {
                    // Setter from
                    params.put(
                            "$T.applyCallback($S, link, entity, $T.class, $T.class, $T::$L, $T::$L, null)",
                            Arrays.asList(
                                    RelationshipManager.class,
                                    String.format("%s -> %s", entity.getSimpleName(), rel.getSimpleName()),
                                    entity,
                                    rel,
                                    rel,
                                    ProcessorUtils.findSetter(processingEnvironment, rel, toSet.getSimpleName()).getSimpleName(),
                                    entity,
                                    ProcessorUtils.findGetter(processingEnvironment, entity, fromSet.getSimpleName()).getSimpleName()
                            )
                    );

                    // Setter to
                    params.put(
                            "$T.applyCallback($S, entity, link, $T.class, $T.class, $T::$L, $T::$L, null)",
                            Arrays.asList(
                                    RelationshipManager.class,
                                    String.format("%s -> %s", rel.getSimpleName(), entity.getSimpleName()),
                                    rel,
                                    entity,
                                    entity,
                                    ProcessorUtils.findSetter(processingEnvironment, entity, fromSet.getSimpleName()).getSimpleName(),
                                    rel,
                                    ProcessorUtils.findGetter(processingEnvironment, rel, toSet.getSimpleName()).getSimpleName()
                            )
                    );
                } else {
                    params.put(
                            "$T.applyCallback($S, link, entity, $T.class, $T.class, $T::$L, (t) -> $L.fromSql(t).$L(), null)",
                            Arrays.asList(
                                    RelationshipManager.class,
                                    String.format("%s -> %s", entity.getSimpleName(), rel.getSimpleName()),
                                    entity,
                                    rel,
                                    rel,
                                    ProcessorUtils.findSetter(processingEnvironment, rel, toSet.getSimpleName()).getSimpleName(),
                                    handleExceptionInfoPropagation(fromSet, toSet, () -> ProcessorUtils.getConverterCaller(processingEnvironment, toSet)),
                                    ProcessorUtils.findGetter(processingEnvironment, entity, fromSet.getSimpleName()).getSimpleName()
                            )
                    );
                }
            }
        }
        params.statements.forEach((e) -> builder.addStatement(
                String.format("rel.getLast().appendThen((entity, link) -> %s)", e.statement), e.params.toArray()));
    }

    private String handleExceptionInfoPropagation(VariableElement fromSet, VariableElement toSet, Supplier<String> converterCallerGetter) {
        try {
            return converterCallerGetter.get();
        } catch (ProcessorException ex) {
            if (toSet.getAnnotation(Converter.class) == null) {
                throw new ProcessorException(String.format("%s referenced by %s.%s", ex.getMessage(), fromSet.getEnclosingElement().getSimpleName(), fromSet.getSimpleName()));
            }
            throw ex;
        }
    }

    private boolean noNeedForConversion(VariableElement fromSet, VariableElement toSet) {
        return fromSet.asType().equals(toSet.asType()); // We apply some implicit conversion during select like string/number conversion
    }

    private EntityEventType[] getEvents(RelationshipAccessor relationship) {
        List<EntityEventType> events = new ArrayList<>();
        for (CascadeType cascadeType : relationship.cascadeTypes) {
            if (cascadeType.equals(CascadeType.ALL)) {
                events.addAll(Arrays.asList(EntityEventType.values()));
            } else if (cascadeType.equals(CascadeType.REMOVE)) {
                events.add(EntityEventType.REMOVE);
            } else if (cascadeType.equals(CascadeType.PERSIST)) {
                events.add(EntityEventType.PERSIST);
            } else if (cascadeType.equals(CascadeType.MERGE)) {
                events.add(EntityEventType.MERGE);
            } else {
                events.add(EntityEventType.UPDATE);
            }
        }

        return events.toArray(new EntityEventType[0]);
    }

    private String asVarArgs(EntityEventType... types) {
        return Stream.of(types)
                .map(Enum::name)
                .map(s -> EntityEventType.class.getName() + "." + s)
                .collect(Collectors.joining(","));
    }

    private void checkBaseDao(TypeElement entity, Set<TypeElement> daoTypes) {
        if (!daoTypes.contains(entity) && !isSkipForIncrementalBuilding()) {
            throw new ProcessorException(String.format("Can't find BaseDao<%s> for Cascade implementation !", entity));
        }
    }

    private boolean isSkipForIncrementalBuilding() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (isIncremental(element.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private boolean isIncremental(String className) {
        return INCREMENTAL_JPS.stream()
                .anyMatch(className::contains);
    }

    private boolean hasRelationships(TypeElement entity) {
        return processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .anyMatch(el -> el.getAnnotation(Cascade.class) != null);
    }

    private static class RelationshipInfo {

        private final TypeElement entity;
        private final List<RelationshipAccessor> annotated;

        private RelationshipInfo(TypeElement entity, List<RelationshipAccessor> annotated) {
            this.entity = entity;
            this.annotated = annotated;
        }
    }

    private static class RelationshipAccessor {

        private final ReturnTypeDefinition returnTypeDefinition;
        private final ExecutableElement getter;
        private final CascadeType[] cascadeTypes;
        private final VariableElement relationship;

        private RelationshipAccessor(ReturnTypeDefinition returnTypeDefinition,
                                    ExecutableElement getter, CascadeType[] cascadeTypes,
                                     VariableElement relationship) {
            this.returnTypeDefinition = returnTypeDefinition;
            this.getter = getter;
            this.cascadeTypes = cascadeTypes;
            this.relationship = relationship;
        }
    }

    private static class RelationshipStatements {
        private final List<RelationshipStatement> statements;

        public RelationshipStatements() {
            this.statements = new ArrayList<>();
        }

        public void put(String statement, List<Object> params) {
            this.statements.add(new RelationshipStatement(statement, params));
        }
    }

    private static class RelationshipStatement {
        private final String statement;
        private final List<Object> params;

        public RelationshipStatement(String statement, List<Object> params) {
            this.statement = statement;
            this.params = params;
        }
    }
}
