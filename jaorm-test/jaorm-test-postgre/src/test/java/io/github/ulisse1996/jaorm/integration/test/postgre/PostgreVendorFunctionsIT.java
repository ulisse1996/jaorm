package io.github.ulisse1996.jaorm.integration.test.postgre;

import io.github.ulisse1996.jaorm.Selectable;
import io.github.ulisse1996.jaorm.integration.test.VendorFunctionsIT;
import io.github.ulisse1996.jaorm.vendor.VendorFunction;
import io.github.ulisse1996.jaorm.vendor.postgre.functions.*;
import org.assertj.core.util.TriFunction;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.function.BiFunction;

public class PostgreVendorFunctionsIT extends VendorFunctionsIT {

    @Override
    protected VendorFunction<Date> getCurrentDateFn() {
        return CurrentDateFunction.INSTANCE;
    }

    @Override
    protected VendorFunction<Timestamp> getCurrentTimestampFn() {
        return CurrentTimestampFunction.INSTANCE;
    }

    @Override
    protected VendorFunction<Time> getTimeFn() {
        return CurrentTimeFunction.INSTANCE;
    }

    @SafeVarargs
    @Override
    protected final VendorFunction<String> getConcatFn(Selectable<String>... selectables) {
        return ConcatFunction.concat(selectables);
    }

    @Override
    protected BiFunction<Integer, Selectable<String>, VendorFunction<String>> getSubstringFn() {
        return (length, value) -> SubstringFunction.substring(value, 1, length);
    }

    @Override
    protected BiFunction<Type, Selectable<String>, VendorFunction<String>> getTrimFn() {
        return (type, value) -> TrimFunction.trim(TrimType.valueOf(type.name()), value);
    }

    @Override
    protected TriFunction<Type, Selectable<String>, Character, VendorFunction<String>> getTrimWithCharFn() {
        return (type, value, character) -> TrimFunction.trim(TrimType.valueOf(type.name()), character, value);
    }
}
