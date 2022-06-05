package io.github.ulisse1996.jaorm.entity;

import io.github.ulisse1996.jaorm.Arguments;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.Sort;
import io.github.ulisse1996.jaorm.cache.Cacheable;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.spi.QueryRunner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class EntityRecordDao<R> implements BaseDao<R> {

    private final Class<R> klass;

    public EntityRecordDao(Class<R> klass) {
        this.klass = klass;
    }

    public static <R> EntityRecordDao<R> getInstance(Class<R> klass) {
        return new EntityRecordDao<>(klass);
    }

    @Override
    public R read(R arg0) {
        Objects.requireNonNull(arg0, "Entity can't be null !");
        Arguments arguments = DelegatesService.getInstance().asWhere(arg0);
        return Cacheable.getCached(this.klass, arguments, () -> QueryRunner.getInstance(this.klass).read(this.klass, DelegatesService.getInstance().getSql(this.klass), argumentsAsParameters(arguments.getValues())));
    }

    @Override
    public List<R> readAll() {
        throw new UnsupportedOperationException("Can't use readAll with Active Record");
    }

    @Override
    public Page<R> page(int page, int size, List<Sort<R>> sorts) {
        throw new UnsupportedOperationException("Can't use Page with Active Record");
    }

    @Override
    public Optional<R> readOpt(R arg0) {
        Objects.requireNonNull(arg0, "Entity can't be null !");
        Arguments arguments = DelegatesService.getInstance().asWhere(arg0);
        return Cacheable.getCachedOpt(this.klass, arguments, () -> QueryRunner.getInstance(this.klass).readOpt(this.klass, DelegatesService.getInstance().getSql(this.klass), argumentsAsParameters(arguments.getValues())).toOptional());
    }
}
