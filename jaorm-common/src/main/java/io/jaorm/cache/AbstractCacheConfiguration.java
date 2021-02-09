package io.jaorm.cache;

import io.jaorm.Arguments;
import io.jaorm.QueryRunner;
import io.jaorm.entity.DelegatesService;
import io.jaorm.entity.sql.SqlParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCacheConfiguration {

    protected abstract <T> JaormCache<T> getCache(Class<T> klass);
    protected abstract <T> JaormAllCache<T> getAllCache(Class<T> klass);

    protected <T> T read(Class<T> klass, Arguments arguments) {
        return QueryRunner.getInstance(klass)
                .read(klass, DelegatesService.getCurrent().getSql(klass), asParameters(arguments));
    }

    protected <T> List<T> readAll(Class<T> klass) {
        return QueryRunner.getInstance(klass)
                .readAll(klass, DelegatesService.getCurrent().getSimpleSql(klass), Collections.emptyList());
    }

    private List<SqlParameter> asParameters(Arguments arguments) {
        return Arrays.stream(arguments.getValues())
                .map(SqlParameter::new)
                .collect(Collectors.toList());
    }
}
