package io.jaorm.entity.sql;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;

public enum SqlAccessor {

    BYTE(byte.class, ResultSet::getByte, (pr, index, val) -> pr.setByte(index, (byte) val)),
    BYTE_WRAPPER(Byte.class, ResultSet::getByte, (pr, index, val) -> pr.setByte(index, (Byte) val)),
    SHORT(short.class, ResultSet::getShort, (pr, index, val) -> pr.setShort(index, (short) val)),
    SHORT_WRAPPER(Short.class, ResultSet::getShort, (pr, index, val) -> pr.setShort(index, (Short) val)),
    INTEGER(int.class, ResultSet::getInt, (pr, index, val) -> pr.setInt(index, (int) val)),
    INTEGER_WRAPPER(Integer.class, ResultSet::getInt, (pr, index, val) -> pr.setInt(index, (Integer) val)),
    LONG(long.class, ResultSet::getLong, (pr, index, val) -> pr.setLong(index, (long) val)),
    LONG_WRAPPER(Long.class, ResultSet::getLong, (pr, index, val) -> pr.setLong(index, (Long) val)),
    FLOAT(float.class, ResultSet::getFloat, (pr, index, val) -> pr.setFloat(index, (float) val)),
    FLOAT_WRAPPER(Float.class, ResultSet::getFloat, (pr, index, val) -> pr.setFloat(index, (Float) val)),
    DOUBLE(double.class, ResultSet::getDouble, (pr, index, val) -> pr.setDouble(index, (double) val)),
    DOUBLE_WRAPPER(Double.class, ResultSet::getDouble, (pr, index, val) -> pr.setDouble(index, (Double) val)),
    STRING(String.class, ResultSet::getString, (pr, index, val) -> pr.setString(index, (String) val)),
    BOOLEAN(boolean.class, ResultSet::getBoolean, (pr, index, val) -> pr.setBoolean(index, (boolean) val)),
    BOOLEAN_WRAPPER(Boolean.class, ResultSet::getBoolean, (pr, index, val) -> pr.setBoolean(index, (Boolean) val)),
    ARRAY(Array.class, ResultSet::getArray, (pr, index, val) -> pr.setArray(index, (Array) val)),
    STREAM(InputStream.class, ResultSet::getAsciiStream, (pr, index, val) -> pr.setAsciiStream(index, (InputStream) val)),
    BLOB(Blob.class, ResultSet::getBlob, (pr, index, val) -> pr.setBlob(index, (Blob) val)),
    BYTES(byte[].class, ResultSet::getBytes, (pr, index, val) -> pr.setBytes(index, (byte[]) val)),
    NCLOB(NClob.class, ResultSet::getNClob, (pr, index, val) -> pr.setNClob(index, (NClob) val)),
    XML(SQLXML.class, ResultSet::getSQLXML, (pr, index, val) -> pr.setSQLXML(index, (SQLXML) val)),
    TIME(Time.class, ResultSet::getTime, (pr, index, val) -> pr.setTime(index, (Time) val)),
    TIMESTAMP(Timestamp.class, ResultSet::getTimestamp, (pr, index, val) -> pr.setTimestamp(index, (Timestamp) val)),
    URL(java.net.URL.class, ResultSet::getURL, (pr, index, val) -> pr.setURL(index, (java.net.URL) val)),
    DATE(java.sql.Date.class, ResultSet::getDate, (pr, index, val) -> pr.setDate(index, (Date) val)),
    DATE_UTIL(java.util.Date.class, ResultSet::getDate, (pr, index, val) -> pr.setDate(index, new java.sql.Date(((java.util.Date) val).getTime()))),
    INSTANT(Instant.class, (rs, col) -> rs.getObject(col, Instant.class), PreparedStatement::setObject),
    OFFSET_DATE_TIME(OffsetDateTime.class, (rs, col) -> rs.getObject(col, OffsetDateTime.class), PreparedStatement::setObject),
    ZONED_DATE_TIME(ZonedDateTime.class, (rs, col) -> rs.getObject(col, ZonedDateTime.class), PreparedStatement::setObject),
    LOCAL_DATE_TIME(LocalDateTime.class, (rs, col) -> rs.getObject(col, LocalDateTime.class), PreparedStatement::setObject),
    LOCAL_DATE(LocalDate.class, (rs, col) -> rs.getObject(col, LocalDate.class), PreparedStatement::setObject),
    LOCAL_TIME(LocalTime.class, (rs, col) -> rs.getObject(col, LocalTime.class), PreparedStatement::setObject),
    OFFSET_TIME(OffsetTime.class, (rs, col) -> rs.getObject(col, OffsetTime.class), PreparedStatement::setObject),
    BIG_DECIMAL(BigDecimal.class, ResultSet::getBigDecimal, (pr, index, val) -> pr.setBigDecimal(index, (BigDecimal) val)),
    NULL(void.class, (rs, colName) -> null, (pr, index, val) -> pr.setNull(index, JDBCType.NULL.getVendorTypeNumber()));

    private final Class<?> klass;
    private final SqlGetter<Object> getter;
    private final SqlSetter<Object> setter;

    SqlAccessor(Class<?> klass, SqlGetter<Object> getter, SqlSetter<Object> setter) {
        this.klass = klass;
        this.getter = getter;
        this.setter = setter;
    }

    public SqlGetter<Object> getGetter() {
        return getter;
    }

    public SqlSetter<Object> getSetter() {
        return setter;
    }

    public static <R> SqlAccessor find(Class<R> klass) {
        for (SqlAccessor accessor : values()) {
            if (!klass.equals(Object.class) && klass.equals(accessor.klass)) {
                return accessor;
            }
        }

        throw new IllegalArgumentException("Can't find accessor for type " + klass);
    }
}
