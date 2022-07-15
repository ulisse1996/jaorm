package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.SqlUtil;
import io.github.ulisse1996.jaorm.annotation.CustomGenerator;
import io.github.ulisse1996.jaorm.entity.converter.ParameterConverter;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.entity.sql.SqlAccessor;
import io.github.ulisse1996.jaorm.logger.JaormLogger;
import io.github.ulisse1996.jaorm.vendor.VendorSpecific;
import io.github.ulisse1996.jaorm.vendor.specific.LockSpecific;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenerationInfo {

    private static final JaormLogger logger = JaormLogger.getLogger(GenerationInfo.class);

    private final String columnName;
    private final String keyColumn;
    private final String valueColumn;
    private final String matchKey;
    private final ParameterConverter converter;
    private final String tableName;
    private final CustomGenerator<?> generator;

    public GenerationInfo(String columnName, CustomGenerator<?> generator) {
        this(columnName, null, null, null, null, ParameterConverter.NONE, generator);
    }

    public GenerationInfo(String columnName, String keyColumn,
                          String valueColumn, String matchKey,
                          String tableName, ParameterConverter converter, CustomGenerator<?> generator) {
        this.columnName = columnName;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.matchKey = matchKey;
        this.converter = converter;
        this.generator = generator;
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    @SuppressWarnings("unchecked")
    public <T> T generate(Class<?> entityClass, Class<?> columnClass) throws SQLException {
        if (generator != null) {
            return (T) generator.generate(entityClass, columnClass, columnName);
        } else {
            return getValue(columnClass);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(Class<?> columnClass) throws SQLException {
        String sql = getSqlLock(String.format(" WHERE %s = ?", keyColumn));
        ResultSet rs = null;
        try (Connection connection = DataSourceProvider.getCurrent().getConnection();
             PreparedStatement pr = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_UPDATABLE)) {
            pr.setObject(1, converter.toValue(matchKey));
            rs = pr.executeQuery();
            if (rs.next()) {
                T value = (T) SqlAccessor.find(columnClass)
                        .getGetter()
                        .get(rs, valueColumn);
                rs.updateObject(valueColumn, addToValue(value));
                rs.updateRow();
                return value;
            } else {
                throw new IllegalArgumentException("Can't retrieve next value for generation !");
            }
        } finally {
            SqlUtil.silentClose(rs);
        }
    }

    protected  <T> Object addToValue(T value) {
        if (value instanceof BigInteger) {
            return ((BigInteger) value).add(BigInteger.ONE);
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).add(BigDecimal.ONE);
        } else if (value instanceof Number) {
            return checkNumber((Number) value);
        } else {
            return new BigDecimal(value.toString()).add(BigDecimal.ONE);
        }
    }

    private Object checkNumber(Number value) {
        if (value instanceof Long) {
            return value.longValue() + 1L;
        } else if (value instanceof Integer) {
            return value.intValue() + 1;
        } else if (value instanceof Double) {
            return value.doubleValue() + 1.0d;
        } else {
            return value.floatValue() + 1.0f;
        }
    }

    protected String getSqlLock(String where) {
        try {
            // We also need keyColumn for Postgre update lock that needs primary key in select
            return VendorSpecific.getSpecific(LockSpecific.class)
                    .selectWithLock(tableName, where, valueColumn, keyColumn);
        } catch (Exception ex) {
            logger.info("Can't find specific for lock type , please contact author"::toString);
        }

        return String.format("SELECT %s FROM %s %s FOR UPDATE", valueColumn, tableName, where);
    }
}
