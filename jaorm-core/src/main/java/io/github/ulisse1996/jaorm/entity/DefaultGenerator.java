package io.github.ulisse1996.jaorm.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DefaultGenerator {

    private DefaultGenerator() {}

    @SuppressWarnings("unchecked")
    public static <T> T forNumeric(Class<T> klass, double numeric) {
        if (BigDecimal.class.equals(klass)) {
            return (T) BigDecimal.valueOf(numeric);
        } else if (BigInteger.class.equals(klass)) {
            return (T) BigInteger.valueOf((long) numeric);
        } else {
            T result = parsePrimitive(klass, numeric);
            if (result != null) return result;
        }

        throw klassNotValid(klass);
    }

    private static <T> IllegalArgumentException klassNotValid(Class<T> klass) {
        return new IllegalArgumentException("Can't find valid default generation for " + klass);
    }

    @SuppressWarnings("unchecked")
    private static <T> T parsePrimitive(Class<T> klass, double numeric) {
        if (byte.class.equals(klass) || Byte.class.isAssignableFrom(klass)) {
            return (T) Byte.valueOf((byte) numeric);
        } else if (short.class.equals(klass) || Short.class.isAssignableFrom(klass)) {
            return (T) Short.valueOf((short) numeric);
        } else if (int.class.equals(klass) || Integer.class.isAssignableFrom(klass)) {
            return (T) Integer.valueOf((int) numeric);
        } else if (long.class.equals(klass) || Long.class.isAssignableFrom(klass)) {
            return (T) Long.valueOf((long) numeric);
        } else if (float.class.equals(klass) || Float.class.isAssignableFrom(klass)) {
            return (T) Float.valueOf((float) numeric);
        } else if (double.class.equals(klass) || Double.class.isAssignableFrom(klass)) {
            return (T) Double.valueOf(numeric);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T forTemporal(Class<T> klass, String format, String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault());
        TemporalAccessor temporalAccessor = formatter.parse(value);
        if (Date.class.equals(klass)) {
            return (T) new Date(temporalAccessor.getLong(ChronoField.MILLI_OF_SECOND));
        } else if (java.util.Date.class.equals(klass)) {
            return (T) new java.util.Date(temporalAccessor.getLong(ChronoField.MILLI_OF_SECOND));
        } else if (Time.class.equals(klass)) {
            return (T) new Time(temporalAccessor.getLong(ChronoField.MILLI_OF_SECOND));
        } else if (Timestamp.class.equals(klass)) {
            return (T) new Timestamp(temporalAccessor.getLong(ChronoField.MILLI_OF_SECOND));
        } else if (Instant.class.equals(klass)) {
            temporalAccessor = formatter.withZone(ZoneId.systemDefault())
                    .parse(value);
            return (T) Instant.from(temporalAccessor);
        } else if (LocalDateTime.class.equals(klass)) {
            return (T) LocalDateTime.from(temporalAccessor);
        } else if (LocalDate.class.equals(klass)) {
            return (T) LocalDate.from(temporalAccessor);
        } else if (LocalTime.class.equals(klass)) {
            return (T) LocalTime.from(temporalAccessor);
        }

        throw klassNotValid(klass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T forTemporal(Class<T> klass) {
        if (Date.class.equals(klass)) {
            return (T) new Date(System.currentTimeMillis());
        } else if (java.util.Date.class.equals(klass)) {
            return (T) new java.util.Date();
        } else if (Time.class.equals(klass)) {
            return (T) new Time(System.currentTimeMillis());
        } else if (Timestamp.class.equals(klass)) {
            return (T) new Timestamp(System.currentTimeMillis());
        } else if (Instant.class.equals(klass)) {
            return (T) Instant.now();
        } else if (OffsetDateTime.class.equals(klass)) {
            return (T) OffsetDateTime.now();
        } else if (ZonedDateTime.class.equals(klass)) {
            return (T) ZonedDateTime.now();
        } else if (LocalDateTime.class.equals(klass)) {
            return (T) LocalDateTime.now();
        } else if (LocalDate.class.equals(klass)) {
            return (T) LocalDate.now();
        } else if (LocalTime.class.equals(klass)) {
            return (T) LocalTime.now();
        } else if (OffsetTime.class.equals(klass)) {
            return (T) OffsetTime.now();
        }

        throw klassNotValid(klass);
    }
}
