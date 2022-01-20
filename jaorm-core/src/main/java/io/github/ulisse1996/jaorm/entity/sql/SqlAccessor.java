package io.github.ulisse1996.jaorm.entity.sql;

import io.github.ulisse1996.jaorm.spi.ConverterService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class SqlAccessor {

    private static final SqlAccessor BYTE = new SqlAccessor(byte.class, ResultSet::getByte, (pr, index, val) -> pr.setByte(index, (byte) val)){};
    private static final SqlAccessor BYTE_WRAPPER = new SqlAccessor(Byte.class, ResultSet::getByte, (pr, index, val) -> pr.setByte(index, (Byte) val)){};
    private static final SqlAccessor SHORT = new SqlAccessor(short.class, ResultSet::getShort, (pr, index, val) -> pr.setShort(index, (short) val)){};
    private static final SqlAccessor SHORT_WRAPPER = new SqlAccessor(Short.class, ResultSet::getShort, (pr, index, val) -> pr.setShort(index, (Short) val)){};
    private static final SqlAccessor INTEGER = new SqlAccessor(int.class, ResultSet::getInt, (pr, index, val) -> pr.setInt(index, (int) val)){};
    private static final SqlAccessor INTEGER_WRAPPER = new SqlAccessor(Integer.class, ResultSet::getInt, (pr, index, val) -> pr.setInt(index, (Integer) val)){};
    private static final SqlAccessor LONG = new SqlAccessor(long.class, ResultSet::getLong, (pr, index, val) -> pr.setLong(index, (long) val)){};
    private static final SqlAccessor LONG_WRAPPER = new SqlAccessor(Long.class, ResultSet::getLong, (pr, index, val) -> pr.setLong(index, (Long) val)){};
    private static final SqlAccessor FLOAT = new SqlAccessor(float.class, ResultSet::getFloat, (pr, index, val) -> pr.setFloat(index, (float) val)){};
    private static final SqlAccessor FLOAT_WRAPPER = new SqlAccessor(Float.class, ResultSet::getFloat, (pr, index, val) -> pr.setFloat(index, (Float) val)){};
    private static final SqlAccessor DOUBLE = new SqlAccessor(double.class, ResultSet::getDouble, (pr, index, val) -> pr.setDouble(index, (double) val)){};
    private static final SqlAccessor DOUBLE_WRAPPER = new SqlAccessor(Double.class, ResultSet::getDouble, (pr, index, val) -> pr.setDouble(index, (Double) val)){};
    private static final SqlAccessor STRING = new SqlAccessor(String.class, ResultSet::getString, (pr, index, val) -> pr.setString(index, (String) val)){};
    private static final SqlAccessor BOOLEAN = new SqlAccessor(boolean.class, ResultSet::getBoolean, (pr, index, val) -> pr.setBoolean(index, (boolean) val)){};
    private static final SqlAccessor BOOLEAN_WRAPPER = new SqlAccessor(Boolean.class, ResultSet::getBoolean, (pr, index, val) -> pr.setBoolean(index, (Boolean) val)){};
    private static final SqlAccessor ARRAY = new SqlAccessor(Array.class, ResultSet::getArray, (pr, index, val) -> pr.setArray(index, (Array) val)){};
    private static final SqlAccessor STREAM = new SqlAccessor(InputStream.class, ResultSet::getAsciiStream, (pr, index, val) -> pr.setAsciiStream(index, (InputStream) val)){};
    private static final SqlAccessor BLOB = new SqlAccessor(Blob.class, ResultSet::getBlob, (pr, index, val) -> pr.setBlob(index, (Blob) val)){};
    private static final SqlAccessor BYTES = new SqlAccessor(byte[].class, ResultSet::getBytes, (pr, index, val) -> pr.setBytes(index, (byte[]) val)){};
    private static final SqlAccessor NCLOB = new SqlAccessor(NClob.class, ResultSet::getNClob, (pr, index, val) -> pr.setNClob(index, (NClob) val)){};
    private static final SqlAccessor XML = new SqlAccessor(SQLXML.class, ResultSet::getSQLXML, (pr, index, val) -> pr.setSQLXML(index, (SQLXML) val)){};
    private static final SqlAccessor TIME = new SqlAccessor(Time.class, ResultSet::getTime, (pr, index, val) -> pr.setTime(index, (Time) val)){};
    private static final SqlAccessor TIMESTAMP = new SqlAccessor(Timestamp.class, ResultSet::getTimestamp, (pr, index, val) -> pr.setTimestamp(index, (Timestamp) val)){};
    private static final SqlAccessor URL = new SqlAccessor(java.net.URL.class, ResultSet::getURL, (pr, index, val) -> pr.setURL(index, (java.net.URL) val)){};
    private static final SqlAccessor DATE = new SqlAccessor(java.sql.Date.class, ResultSet::getDate, (pr, index, val) -> pr.setDate(index, (Date) val)){};
    private static final SqlAccessor DATE_UTIL = new SqlAccessor(java.util.Date.class, ResultSet::getDate, (pr, index, val) -> pr.setDate(index, new java.sql.Date(((java.util.Date) val).getTime()))){};
    private static final SqlAccessor INSTANT = new SqlAccessor(Instant.class, (rs, col) -> rs.getObject(col, Instant.class), PreparedStatement::setObject){};
    private static final SqlAccessor OFFSET_DATE_TIME = new SqlAccessor(OffsetDateTime.class, (rs, col) -> rs.getObject(col, OffsetDateTime.class), PreparedStatement::setObject){};
    private static final SqlAccessor ZONED_DATE_TIME = new SqlAccessor(ZonedDateTime.class, (rs, col) -> rs.getObject(col, ZonedDateTime.class), PreparedStatement::setObject){};
    private static final SqlAccessor LOCAL_DATE_TIME = new SqlAccessor(LocalDateTime.class, (rs, col) -> rs.getObject(col, LocalDateTime.class), PreparedStatement::setObject){};
    private static final SqlAccessor LOCAL_DATE = new SqlAccessor(LocalDate.class, (rs, col) -> rs.getObject(col, LocalDate.class), PreparedStatement::setObject){};
    private static final SqlAccessor LOCAL_TIME = new SqlAccessor(LocalTime.class, (rs, col) -> rs.getObject(col, LocalTime.class), PreparedStatement::setObject){};
    private static final SqlAccessor OFFSET_TIME = new SqlAccessor(OffsetTime.class, (rs, col) -> rs.getObject(col, OffsetTime.class), PreparedStatement::setObject){};
    private static final SqlAccessor BIG_DECIMAL = new SqlAccessor(BigDecimal.class, ResultSet::getBigDecimal, (pr, index, val) -> pr.setBigDecimal(index, (BigDecimal) val)){};
    private static final SqlAccessor BIG_INTEGER = new SqlAccessor(BigInteger.class, (rs, col) -> {
        BigDecimal bigDecimal = rs.getBigDecimal(col);
        return Optional.ofNullable(bigDecimal).map(b -> BigInteger.valueOf(b.longValue())).orElse(null);
    }, (pr, index, val) -> {
        BigInteger b = (BigInteger) val;
        if (b != null) {
            pr.setBigDecimal(index, BigDecimal.valueOf(b.longValue()));
        } else {
            pr.setBigDecimal(index, null);
        }
    }){};
    public static final SqlAccessor NULL = new SqlAccessor(void.class, (rs, colName) -> null, (pr, index, val) -> pr.setNull(index, JDBCType.NULL.getVendorTypeNumber())){};

    private static final List<SqlAccessor> ALL = Arrays.asList(
            BYTE, BYTE_WRAPPER, SHORT, SHORT_WRAPPER, INTEGER, INTEGER_WRAPPER,
            LONG, LONG_WRAPPER, FLOAT, FLOAT_WRAPPER, DOUBLE, DOUBLE_WRAPPER,
            STRING, BOOLEAN, BOOLEAN_WRAPPER, ARRAY, STREAM, BLOB,
            BYTES, NCLOB, XML, TIME, TIMESTAMP, URL, DATE, DATE_UTIL,
            INSTANT, OFFSET_DATE_TIME, ZONED_DATE_TIME, LOCAL_DATE_TIME,
            LOCAL_DATE, LOCAL_TIME, OFFSET_TIME, BIG_DECIMAL, BIG_INTEGER, NULL
    );

    private final Class<?> klass;
    private final SqlGetter<Object> getter;
    private final SqlSetter<Object> setter;

    protected SqlAccessor(Class<?> klass, SqlGetter<Object> getter, SqlSetter<Object> setter) {
        this.klass = klass;
        this.getter = getter;
        this.setter = setter;
    }

    private static List<SqlAccessor> values() {
        return ALL;
    }

    public final SqlGetter<Object> getGetter() {
        return getter;
    }

    public final SqlSetter<Object> getSetter() {
        return setter;
    }

    public static <R> SqlAccessor find(Class<R> klass) {
        for (SqlAccessor accessor : values()) {
            if (!klass.equals(Object.class) && klass.equals(accessor.klass)) {
                return accessor;
            }
        }

        // We can't find sql accessor , try with custom feature
        SqlAccessor customAccessor = ConverterService.getInstance().findConverter(klass);
        if (customAccessor != null) {
            return customAccessor;
        }

        throw new IllegalArgumentException("Can't find accessor for type " + klass);
    }
}
