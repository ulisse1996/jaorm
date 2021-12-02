package io.github.ulisse1996.jaorm.tools.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class TableMetadata {

    private final List<ColumnMetadata> columns;

    public TableMetadata(ResultSetMetaData metaData) throws SQLException {
        List<ColumnMetadata> values = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            values.add(new ColumnMetadata(
                    metaData.getColumnName(i + 1),
                    JDBCType.valueOf(metaData.getColumnType(i + 1))
            ));
        }
        this.columns = Collections.unmodifiableList(values);
    }

    public Optional<ColumnMetadata> findColumn(String name) {
        return this.columns.stream()
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public static class ColumnMetadata {
        private final String name;
        private final List<Class<?>> supportedTypes;

        public ColumnMetadata(String columnName, JDBCType type) {
            this.name = columnName;
            this.supportedTypes = ColumnConversion.from(type).classes;
        }

        public boolean matchType(String klass) {
            return this.getSupportedTypes().stream().anyMatch(c -> c.getName().equalsIgnoreCase(klass)
                    || (c.isArray() && c.getComponentType().getName().equalsIgnoreCase(klass)));
        }

        public List<Class<?>> getSupportedTypes() {
            return this.supportedTypes;
        }

        public List<String> getSupportedTypesName() {
            return this.supportedTypes.stream()
                    .map(c -> c.isArray() ? c.getComponentType() + "[]" : c.getName())
                    .collect(Collectors.toList());
        }
    }

    private enum ColumnConversion {
        STRING(String.class),
        DOUBLE(Double.class, double.class),
        FLOAT(
                Float.class,
                float.class
        ),
        LONG(Long.class, long.class),
        INTEGER(
                Integer.class,
                int.class
        ),
        SHORT(Short.class, short.class),
        BOOLEAN(
                Boolean.class,
                boolean.class
        ),
        CHAR(Character.class, char.class, String.class),
        NUMERIC(
                BigDecimal.class,
                Float.class,
                float.class,
                Double.class,
                double.class,
                Long.class,
                long.class,
                Integer.class,
                int.class,
                Short.class,
                short.class
        ),
        DATE(
                java.util.Date.class,
                Date.class
        ),
        TIMESTAMP(Timestamp.class, java.util.Date.class, Date.class),
        TIME(Time.class),
        BINARY(
                byte[].class
        ),
        DECIMAL(
                BigDecimal.class,
                Float.class,
                float.class,
                Double.class,
                double.class
        ),
        NULL(Void.TYPE);

        private final List<Class<?>> classes;

        ColumnConversion(Class<?>... classes) {
            this.classes = Arrays.asList(classes);
        }

        private static ColumnConversion from(JDBCType jdbcType) {
            switch (jdbcType) {
                case BIT:
                case BOOLEAN:
                    return BOOLEAN;
                case TINYINT:
                case SMALLINT:
                    return SHORT;
                case INTEGER:
                    return INTEGER;
                case BIGINT:
                    return LONG;
                case FLOAT:
                    return FLOAT;
                case REAL:
                case DOUBLE:
                    return DOUBLE;
                case NUMERIC:
                    return NUMERIC;
                case DECIMAL:
                    return DECIMAL;
                case CHAR:
                    return CHAR;
                case VARCHAR:
                case LONGVARCHAR:
                case CLOB:
                case NCHAR:
                case NVARCHAR:
                case LONGNVARCHAR:
                case NCLOB:
                    return STRING;
                case DATE:
                    return DATE;
                case TIME:
                case TIME_WITH_TIMEZONE:
                    return TIME;
                case TIMESTAMP:
                case TIMESTAMP_WITH_TIMEZONE:
                    return TIMESTAMP;
                case BINARY:
                case VARBINARY:
                case LONGVARBINARY:
                case BLOB:
                    return BINARY;
                default:
                    return NULL;
            }
        }
    }
}
