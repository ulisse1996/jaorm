package io.github.ulisse1996.jaorm.cache;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;
import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCacheConfiguration {

    protected abstract <T> JaormCache<T> getCache(Class<T> klass);
    protected abstract <T> JaormAllCache<T> getAllCache(Class<T> klass);

    protected <T> T read(Class<T> klass, Arguments arguments) {
        return QueryRunner.getInstance(klass)
                .read(klass, DelegatesService.getInstance().getSql(klass), asParameters(arguments));
    }

    protected <T> List<T> readAll(Class<T> klass) {
        return QueryRunner.getInstance(klass)
                .readAll(klass, DelegatesService.getInstance().getSimpleSql(klass), Collections.emptyList());
    }

    private List<SqlParameter> asParameters(Arguments arguments) {
        return Arrays.stream(arguments.getValues())
                .map(SqlParameter::new)
                .collect(Collectors.toList());
    }
}
