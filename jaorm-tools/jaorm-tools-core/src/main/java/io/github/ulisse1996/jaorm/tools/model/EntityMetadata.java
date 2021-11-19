package io.github.ulisse1996.jaorm.tools.model;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;
import io.github.ulisse1996.jaorm.tools.util.ValidationUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EntityMetadata {

    private static final Map<String, Class<?>> PRIMITIVE_MAP;
    static {
        Map<String, Class<?>> map = new HashMap<>();
        map.put("byte", byte.class);
        map.put("short", short.class);
        map.put("int", int.class);
        map.put("long", long.class);
        map.put("float", float.class);
        map.put("double", double.class);
        map.put("boolean", boolean.class);
        map.put("char", char.class);
        PRIMITIVE_MAP = Collections.unmodifiableMap(map);
    }

    private final List<FieldMetadata> fields;
    private final String table;

    public EntityMetadata(ClassOrInterfaceDeclaration klass) {
        List<FieldMetadata> values = new ArrayList<>();
        for (FieldDeclaration f : klass.getFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                FieldMetadata fieldMetadata = new FieldMetadata(f);
                values.add(fieldMetadata);
            }
        }
        this.fields = Collections.unmodifiableList(values);
        this.table = null;
    }

    public EntityMetadata(Map.Entry<Class<?>, Supplier<? extends EntityDelegate<?>>> entry) {
        EntityDelegate<?> delegate = entry.getValue().get();
        this.fields = buildFields(delegate.getEntityMapper());
        this.table = delegate.getTable();
    }

    private List<FieldMetadata> buildFields(EntityMapper<?> entityMapper) {
        return entityMapper.getMappers()
                .stream()
                .map(m -> new FieldMetadata(null, m.getType().getName(), m.getName()))
                .collect(Collectors.toList());
    }

    public List<FieldMetadata> getFields() {
        return fields;
    }

    public String getTable() {
        return this.table;
    }

    public static class FieldMetadata {

        private final String name;
        private final String type;
        private final String columnName;
        private String converterType;

        private FieldMetadata(String name, String type, String columnName) {
            this.name = name;
            this.type = type;
            this.columnName = columnName;
        }

        private FieldMetadata(FieldDeclaration field) {
            VariableDeclarator variableDeclarator = field.getVariables().get(0);
            this.name = variableDeclarator.getName().asString();
            type = getClass(variableDeclarator.getType());
            this.columnName = Objects.requireNonNull(ValidationUtils.getExpression(field, Column.class, "name"))
                    .asStringLiteralExpr()
                    .asString();
            Expression value = ValidationUtils.getSingleValueExpression(field, Converter.class);
            if (value != null) {
                ResolvedType resolvedType = value.asClassExpr().getType().resolve()
                        .asReferenceType()
                        .getAllAncestors()
                        .get(0)
                        .getTypeParametersMap()
                        .get(0).b;
                this.converterType = resolvedType.asReferenceType().getQualifiedName();
            }
        }

        public String getConverterType() {
            return converterType;
        }

        private String getClass(Type type) {
            final String currType;
            if (type.isPrimitiveType()) {
                currType = PRIMITIVE_MAP.get(type.asPrimitiveType().getType().asString()).getName();
            } else if (type.isArrayType()) {
                Type component = type.asArrayType().getComponentType();
                return getClass(component);
            } else {
                currType = type.resolve().asReferenceType().getQualifiedName();
            }
            return currType;
        }

        public String getName() {
            return Optional.ofNullable(this.name).orElse(this.columnName);
        }

        public String getType() {
            return type;
        }

        public String getColumnName() {
            return columnName;
        }
    }
}
